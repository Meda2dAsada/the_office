package office;

import office.agents.OfficeAgent;
import office.model.BufferConfig;
import office.model.SimulationConfig;
import office.sync.SimulationController;
import office.sync.TrackedResource;

/**
 * Prueba rapida sin interfaz grafica (headless) para validar:
 * 1) Regla m <= n en todos los buferes y zonas criticas.
 * 2) Interaccion concurrente entre agentes (Cliente, Recepcionista, Vendedor, Supervisor, Administrador).
 * 3) Visibilidad de estados Thread.State y metricas de espera/ocupacion.
 */
public class HeadlessSmokeTest {
    public static void main(String[] args) throws InterruptedException {
        SimulationConfig cfg = new SimulationConfig();
        cfg.setClientCount(3);
        cfg.setReceptionistCount(1);
        cfg.setSellerCount(1);
        cfg.setSupervisorCount(1);
        cfg.setAdminCount(1);
        cfg.setClientArrivalIntervalMs(100);
        cfg.setReceptionTimeMs(120);
        cfg.setSellerQuoteTimeMs(120);
        cfg.setPrinterTimeMs(100);
        cfg.setSupervisorReviewTimeMs(100);
        cfg.setAdminProcessTimeMs(100);
        cfg.setRestDurationMs(150);
        cfg.setAnimationStepMs(10);

        // Validar m <= n en configuracion
        BufferConfig b3 = cfg.getB3ReceptionQueue();
        b3.setN(4);
        b3.setM(10); // Debe ajustarse automaticamente a m <= n (4)
        if (b3.getM() > b3.getN()) {
            throw new IllegalStateException("Fallo validacion m <= n en B3");
        }
        b3.setM(2);

        SimulationController controller = new SimulationController(cfg);
        controller.start();

        long deadline = System.currentTimeMillis() + 12000;
        boolean sawNonRunnableState = false;
        while (System.currentTimeMillis() < deadline) {
            for (OfficeAgent a : controller.getContext().getAgents()) {
                Thread.State st = a.getState();
                if (st == Thread.State.WAITING || st == Thread.State.TIMED_WAITING || st == Thread.State.BLOCKED) {
                    sawNonRunnableState = true;
                }
            }
            if (controller.getContext().getCompletedOrders().size() >= 3) {
                break;
            }
            Thread.sleep(100);
        }

        int completed = controller.getContext().getCompletedOrders().size();
        System.out.println("Pedidos completados: " + completed);
        System.out.println("Detecto estados WAITING/TIMED_WAITING/BLOCKED: " + sawNonRunnableState);
        for (TrackedResource res : controller.getContext().getAllResources()) {
            System.out.println(res.getConfig().getCode() + " (" + res.getConfig().getM() + "/" + res.getConfig().getN()
                    + ") dentro=" + res.getCurrentCount() + " esperando=" + res.getWaitingCount());
        }

        controller.stop();
        Thread.sleep(300);

        if (completed < 3) {
            throw new AssertionError("Se esperaban 3 pedidos completados y hubo: " + completed);
        }
        if (!sawNonRunnableState) {
            throw new AssertionError("No se observaron estados de espera en los hilos");
        }
        System.out.println("SMOKE TEST OK");
    }
}
