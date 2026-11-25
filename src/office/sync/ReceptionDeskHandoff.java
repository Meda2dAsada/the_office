package office.sync;

import office.model.Order;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Punto de encuentro en B1 entre un Cliente y una Recepcionista.
 * El cliente entrega su solicitud, espera validacion y numero de pedido, y luego libera B1.
 */
public class ReceptionDeskHandoff {
    private final AtomicBoolean running;
    private Order pendingOrder;
    private boolean registrationFinished;

    public ReceptionDeskHandoff(AtomicBoolean running) {
        this.running = running;
    }

    /**
     * Llamado por el Cliente cuando ya ocupa B1.
     */
    public synchronized void submitAndWaitRegistration(Order order) throws InterruptedException {
        while (pendingOrder != null && running.get()) {
            wait();
        }
        if (!running.get()) {
            return;
        }
        pendingOrder = order;
        registrationFinished = false;
        notifyAll();

        while (!registrationFinished && running.get()) {
            wait();
        }
        pendingOrder = null;
        notifyAll();
    }

    /**
     * Llamado por la Recepcionista para esperar al siguiente cliente en B1.
     */
    public synchronized Order takeIncomingOrder() throws InterruptedException {
        while ((pendingOrder == null || registrationFinished) && running.get()) {
            wait();
        }
        if (!running.get()) {
            return null;
        }
        return pendingOrder;
    }

    /**
     * Llamado por la Recepcionista al entregar el numero de pedido al cliente para que libere B1.
     */
    public synchronized void confirmRegistered() {
        this.registrationFinished = true;
        notifyAll();
    }

    public synchronized void wakeAll() {
        notifyAll();
    }
}
