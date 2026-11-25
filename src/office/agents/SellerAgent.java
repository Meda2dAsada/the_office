package office.agents;

import office.model.AgentType;
import office.model.BpmnState;
import office.model.OfficeLayout;
import office.model.Order;
import office.sync.OfficeContext;

/**
 * Agente Vendedor (BPMN 03 + 03a + 03b): retira pedidos de B4, calcula cotizacion,
 * imprime en B2 (Semaforo), coloca en B5 (Lock) y puede descansar o trasladarse entre sedes.
 */
public class SellerAgent extends OfficeAgent {
    private static final double PRECIO_HOJA = 1.5;
    private static final double PRECIO_CUADERNO = 25.0;
    private static final double PRECIO_CARPETA = 18.0;

    private int processedCount = 0;
    private volatile boolean transferRequested = false;

    public SellerAgent(OfficeContext context, String agentId, String siteName) {
        super(context, agentId, AgentType.VENDEDOR, siteName, OfficeLayout.SELLER_DESK);
    }

    public void requestTransfer() {
        this.transferRequested = true;
    }

    @Override
    public void run() {
        try {
            while (shouldKeepRunning()) {
                context.checkPause();

                if (transferRequested) {
                    executeTransferToRemoteSite();
                    break;
                }

                setCurrentOrderId("-");
                setBpmnState(BpmnState.ESPERANDO_PEDIDO_B4);
                setDetail("Esperando solicitud en B4");
                moveTo(OfficeLayout.SELLER_DESK);

                Order order = context.getB4RequestTray().take();
                if (order == null || !shouldKeepRunning()) break;

                setCurrentOrderId("#" + order.getId());
                setBpmnState(BpmnState.COTIZANDO);
                double amount = order.getHojas() * PRECIO_HOJA
                        + order.getCuadernos() * PRECIO_CUADERNO
                        + order.getCarpetas() * PRECIO_CARPETA;
                order.setTotalAmount(amount);
                setDetail("Cotizando #" + order.getId() + " ($" + String.format("%.2f", amount) + ")");
                workSleep(context.getConfig().getSellerQuoteTimeMs());
                if (!shouldKeepRunning()) break;

                // Usar impresora B2 (semaforo de capacidad 1)
                setBpmnState(BpmnState.ESPERANDO_IMPRESORA_B2);
                moveTo(OfficeLayout.B2_PRINTER.x - 35, OfficeLayout.B2_PRINTER.y);
                context.getB2Printer().enter();
                try {
                    if (!shouldKeepRunning()) return;
                    setBpmnState(BpmnState.IMPRIMIENDO_B2);
                    setDetail("Imprimiendo cotizacion #" + order.getId());
                    moveTo(OfficeLayout.B2_PRINTER);
                    workSleep(context.getConfig().getPrinterTimeMs());
                } finally {
                    context.getB2Printer().exit();
                }

                if (!shouldKeepRunning()) break;

                // Depositar en B5 (LockBuffer) con B2 ya liberada
                setBpmnState(BpmnState.ESPERANDO_B5);
                moveTo(OfficeLayout.B5_TRAY.x + 25, OfficeLayout.B5_TRAY.y);
                setBpmnState(BpmnState.INSERTANDO_B5);
                context.getB5ReviewTray().put(order);
                context.log(agentId + " envio cotizacion #" + order.getId() + " ($" + String.format("%.2f", amount) + ") a B5.");

                processedCount++;
                if (transferRequested && shouldKeepRunning()) {
                    executeTransferToRemoteSite();
                    break;
                }
                if (processedCount % context.getConfig().getOrdersBeforeRest() == 0 && shouldKeepRunning()) {
                    takeRestInB7(OfficeLayout.SELLER_DESK);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            setBpmnState(BpmnState.FINALIZADO);
        }
    }

    private void executeTransferToRemoteSite() throws InterruptedException {
        setBpmnState(BpmnState.TRASLADANDO_SEDE);
        setDetail("Traslado a otra sede sin recursos ocupados");
        moveTo(OfficeLayout.BRANCH_GATE);
        context.log(agentId + " salio por acceso de " + siteName + " para trasladarse.");
        workSleep(700);
    }
}
