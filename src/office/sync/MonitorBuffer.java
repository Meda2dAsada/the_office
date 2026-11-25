package office.sync;

import office.model.BufferConfig;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bufer productor-consumidor protegido con monitor Java (synchronized / wait / notifyAll).
 * Usado para B4 (Bandeja de solicitudes).
 */
public class MonitorBuffer<T> implements TrackedResource {
    private final BufferConfig config;
    private final Queue<T> queue = new LinkedList<>();
    private final AtomicInteger waitingPut = new AtomicInteger(0);
    private final AtomicInteger waitingTake = new AtomicInteger(0);
    private final AtomicBoolean running;

    public MonitorBuffer(BufferConfig config, AtomicBoolean running) {
        this.config = config;
        this.running = running;
    }

    public void put(T item) throws InterruptedException {
        synchronized (this) {
            boolean counted = false;
            try {
                while (queue.size() >= config.getM() && running.get()) {
                    if (!counted) {
                        waitingPut.incrementAndGet();
                        counted = true;
                    }
                    wait();
                }
                if (!running.get()) {
                    return;
                }
                queue.add(item);
                notifyAll();
            } finally {
                if (counted) {
                    waitingPut.decrementAndGet();
                }
            }
        }
    }

    public T take() throws InterruptedException {
        synchronized (this) {
            boolean counted = false;
            try {
                while (queue.isEmpty() && running.get()) {
                    if (!counted) {
                        waitingTake.incrementAndGet();
                        counted = true;
                    }
                    wait();
                }
                if (!running.get() && queue.isEmpty()) {
                    return null;
                }
                T item = queue.poll();
                notifyAll();
                return item;
            } finally {
                if (counted) {
                    waitingTake.decrementAndGet();
                }
            }
        }
    }

    @Override
    public BufferConfig getConfig() {
        return config;
    }

    @Override
    public synchronized int getCurrentCount() {
        return queue.size();
    }

    @Override
    public int getWaitingCount() {
        return waitingPut.get() + waitingTake.get();
    }

    @Override
    public synchronized void wakeAll() {
        notifyAll();
    }
}
