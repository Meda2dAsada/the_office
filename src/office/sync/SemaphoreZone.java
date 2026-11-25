package office.sync;

import office.model.BufferConfig;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Zona fisica o recurso exclusivo controlado por Semaforo (cupos m de n).
 * Usado para B1 (Puesto de recepcion), B2 (Impresora), B3 (Fila de recepcion) y B7 (Descanso).
 */
public class SemaphoreZone implements TrackedResource {
    private final BufferConfig config;
    private final Semaphore semaphore;
    private final AtomicInteger currentCount = new AtomicInteger(0);
    private final AtomicInteger waitingCount = new AtomicInteger(0);

    public SemaphoreZone(BufferConfig config) {
        this.config = config;
        this.semaphore = new Semaphore(config.getM(), true);
    }

    public void enter() throws InterruptedException {
        waitingCount.incrementAndGet();
        try {
            semaphore.acquire();
            currentCount.incrementAndGet();
        } finally {
            waitingCount.decrementAndGet();
        }
    }

    public void exit() {
        currentCount.updateAndGet(v -> Math.max(0, v - 1));
        semaphore.release();
    }

    @Override
    public BufferConfig getConfig() {
        return config;
    }

    @Override
    public int getCurrentCount() {
        return currentCount.get();
    }

    @Override
    public int getWaitingCount() {
        return waitingCount.get();
    }

    @Override
    public void wakeAll() {
        // Los hilos esperando en acquire() despiertan con interrupt() al detener la simulacion.
    }
}
