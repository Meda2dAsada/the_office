package office.sync;

import office.model.BufferConfig;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Bufer productor-consumidor protegido con ReentrantLock y Condition (notFull / notEmpty).
 * Usado para B5 (Bandeja de revision) y B6 (Bandeja de resultados).
 */
public class LockBuffer<T> implements TrackedResource {
    private final BufferConfig config;
    private final Queue<T> queue = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock(true);
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();
    private final AtomicInteger waitingPut = new AtomicInteger(0);
    private final AtomicInteger waitingTake = new AtomicInteger(0);
    private final AtomicBoolean running;

    public LockBuffer(BufferConfig config, AtomicBoolean running) {
        this.config = config;
        this.running = running;
    }

    public void put(T item) throws InterruptedException {
        lock.lockInterruptibly();
        boolean counted = false;
        try {
            while (queue.size() >= config.getM() && running.get()) {
                if (!counted) {
                    waitingPut.incrementAndGet();
                    counted = true;
                }
                notFull.await();
            }
            if (!running.get()) {
                return;
            }
            queue.add(item);
            notEmpty.signalAll();
        } finally {
            if (counted) {
                waitingPut.decrementAndGet();
            }
            lock.unlock();
        }
    }

    public T take() throws InterruptedException {
        lock.lockInterruptibly();
        boolean counted = false;
        try {
            while (queue.isEmpty() && running.get()) {
                if (!counted) {
                    waitingTake.incrementAndGet();
                    counted = true;
                }
                notEmpty.await();
            }
            if (!running.get() && queue.isEmpty()) {
                return null;
            }
            T item = queue.poll();
            notFull.signalAll();
            return item;
        } finally {
            if (counted) {
                waitingTake.decrementAndGet();
            }
            lock.unlock();
        }
    }

    @Override
    public BufferConfig getConfig() {
        return config;
    }

    @Override
    public int getCurrentCount() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getWaitingCount() {
        return waitingPut.get() + waitingTake.get();
    }

    @Override
    public void wakeAll() {
        lock.lock();
        try {
            notFull.signalAll();
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
