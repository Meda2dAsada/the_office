package office.agents;

import office.model.AgentType;
import office.model.BpmnState;
import office.model.OfficeLayout;
import office.model.Order;
import office.sync.OfficeContext;

/**
 * Agente Administrador (BPMN 05): retira resultados de B6 (Lock), si aprobado imprime comprobante
 * en B2 (Semaforo), si rechazado registra motivo, y notifica al Cliente correspondiente.
 */
public class AdminAgent extends OfficeAgent {
    private int closedCount = 0;

    public AdminAgent(OfficeContext context, String agentId, String siteName) {
        super(context, agentId, AgentType.ADMINISTRADOR, siteName, OfficeLayout.ADMIN_DESK);
    }

    @Override
    public void run() {
        try {
            while (shouldKeepRunning()) {
                context.checkPause();
                setCurrentOrderId("-");
                setBpmnState(BpmnState.ESPERANDO_RESULTADO_B6);
                setDetail("Esperando resultado en B6");
                moveTo(OfficeLayout.ADMIN_DESK);

                Order order = context.getB6ResultTray().take();
                if (order == null || !shouldKeepRunning()) break;

                setCurrentOrderId("#" + order.getId());
                setBpmnState(BpmnState.ELABORANDO_COMPROBANTE);
                setDetail("Registrando decision #" + order.getId());
                workSleep(context.getConfig().getAdminProcessTimeMs());
                if (!shouldKeepRunning()) break;

                if (order.isApproved()) {
                    setBpmnState(BpmnState.ESPERANDO_IMPRESORA_B2);
                    moveTo(OfficeLayout.B2_PRINTER.x - 35, OfficeLayout.B2_PRINTER.y + 25);
                    context.getB2Printer().enter();
                    try {
                        if (!shouldKeepRunning()) return;
                        setBpmnState(BpmnState.IMPRIMIENDO_B2);
                        setDetail("Imprimiendo comprobante #" + order.getId());
                        moveTo(OfficeLayout.B2_PRINTER);
                        workSleep(context.getConfig().getPrinterTimeMs());
                    } finally {
                        context.getB2Printer().exit();
                    }
                    moveTo(OfficeLayout.ADMIN_DESK);
                }

                if (!shouldKeepRunning()) break;
                setBpmnState(BpmnState.NOTIFICANDO_CLIENTE);
                setDetail("Notificando a " + order.getClientId());
                context.recordCompletedOrder(order);
                order.markCompleted();
                context.log(agentId + " cerro pedido #" + order.getId() + " (" + (order.isApproved() ? "APROBADO" : "RECHAZADO") + ").");

                closedCount++;
                if (closedCount % context.getConfig().getOrdersBeforeRest() == 0 && shouldKeepRunning()) {
                    takeRestInB7(OfficeLayout.ADMIN_DESK);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            setBpmnState(BpmnState.FINALIZADO);
        }
    }
}
