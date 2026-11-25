package office.agents;

import office.model.AgentType;
import office.model.BpmnState;
import office.model.OfficeLayout;
import office.model.Order;
import office.sync.OfficeContext;

/**
 * Agente Recepcionista (BPMN 02): atiende clientes en B1, valida y registra pedidos,
 * y los deposita en la bandeja de solicitudes B4 (Monitor).
 */
public class ReceptionistAgent extends OfficeAgent {
    private int handledCount = 0;

    public ReceptionistAgent(OfficeContext context, String agentId, String siteName) {
        super(context, agentId, AgentType.RECEPCIONISTA, siteName, OfficeLayout.RECEPTIONIST_DESK);
    }

    @Override
    public void run() {
        try {
            while (shouldKeepRunning()) {
                context.checkPause();
                setCurrentOrderId("-");
                setBpmnState(BpmnState.ESPERANDO_CLIENTE);
                setDetail("Disponible en recepcion");
                moveTo(OfficeLayout.RECEPTIONIST_DESK);

                Order order = context.getDeskHandoff().takeIncomingOrder();
                if (order == null || !shouldKeepRunning()) break;

                setBpmnState(BpmnState.ATENDIENDO_CLIENTE);
                setDetail("Atendiendo a " + order.getClientId());
                workSleep(context.getConfig().getReceptionTimeMs() / 2);

                setBpmnState(BpmnState.VALIDANDO_PEDIDO);
                if (order.hasValidItems()) {
                    int id = context.nextOrderId();
                    order.setId(id);
                    setCurrentOrderId("#" + id);
                    setDetail("Pedido #" + id + " valido (" + order.getTotalUnits() + " uds)");
                    workSleep(context.getConfig().getReceptionTimeMs() / 2);
                }

                // Entrega numero al cliente para que libere B1 antes de esperar espacio en B4
                context.getDeskHandoff().confirmRegistered();

                if (!shouldKeepRunning()) break;

                // Si no hay pipeline completo configurado (ej. prueba basica de 2 agentes), completar aqui
                boolean hasFullPipeline = context.getConfig().getSellerCount() > 0
                        && context.getConfig().getSupervisorCount() > 0
                        && context.getConfig().getAdminCount() > 0;

                setBpmnState(BpmnState.ESPERANDO_B4);
                moveTo(OfficeLayout.B4_TRAY.x - 20, OfficeLayout.B4_TRAY.y);

                setBpmnState(BpmnState.INSERTANDO_B4);
                context.getB4RequestTray().put(order);
                context.log(agentId + " registro pedido #" + order.getId() + " de " + order.getClientId() + " en B4.");

                if (!hasFullPipeline) {
                    order.setTotalAmount(order.getTotalUnits() * 10.0);
                    order.setApproved(true);
                    order.setReason("Registrado en B4 (modo basico)");
                    context.recordCompletedOrder(order);
                    order.markCompleted();
                }

                handledCount++;
                if (handledCount % context.getConfig().getOrdersBeforeRest() == 0 && shouldKeepRunning()) {
                    takeRestInB7(OfficeLayout.RECEPTIONIST_DESK);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            setBpmnState(BpmnState.FINALIZADO);
        }
    }
}
