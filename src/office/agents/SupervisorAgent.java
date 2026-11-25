package office.agents;

import office.model.AgentType;
import office.model.BpmnState;
import office.model.OfficeLayout;
import office.model.Order;
import office.sync.OfficeContext;

/**
 * Agente Supervisor (BPMN 04): retira cotizaciones de B5 (Lock), aprueba si total unidades <= limite
 * (50 inicial configurable) o rechaza con motivo, y deposita en B6 (Lock).
 */
public class SupervisorAgent extends OfficeAgent {
    private int reviewedCount = 0;

    public SupervisorAgent(OfficeContext context, String agentId, String siteName) {
        super(context, agentId, AgentType.SUPERVISOR, siteName, OfficeLayout.SUPERVISOR_DESK);
    }

    @Override
    public void run() {
        try {
            while (shouldKeepRunning()) {
                context.checkPause();
                setCurrentOrderId("-");
                setBpmnState(BpmnState.ESPERANDO_REVISION_B5);
                setDetail("Esperando cotizacion en B5");
                moveTo(OfficeLayout.SUPERVISOR_DESK);

                Order order = context.getB5ReviewTray().take();
                if (order == null || !shouldKeepRunning()) break;

                setCurrentOrderId("#" + order.getId());
                setBpmnState(BpmnState.REVISANDO_PEDIDO);
                int limit = context.getConfig().getSupervisorUnitLimit();
                int total = order.getTotalUnits();
                setDetail("Revisando #" + order.getId() + " (" + total + "/" + limit + " uds)");
                workSleep(context.getConfig().getSupervisorReviewTimeMs());
                if (!shouldKeepRunning()) break;

                if (total <= limit) {
                    order.setApproved(true);
                    order.setReason("Aprobado (<= " + limit + " uds)");
                } else {
                    order.setApproved(false);
                    order.setReason("Excede limite de " + limit + " uds (" + total + ")");
                }

                setBpmnState(BpmnState.ESPERANDO_B6);
                moveTo(OfficeLayout.B6_TRAY.x - 20, OfficeLayout.B6_TRAY.y);
                setBpmnState(BpmnState.INSERTANDO_B6);
                context.getB6ResultTray().put(order);
                context.log(agentId + " reviso #" + order.getId() + ": " + order.getReason());

                reviewedCount++;
                if (reviewedCount % context.getConfig().getOrdersBeforeRest() == 0 && shouldKeepRunning()) {
                    takeRestInB7(OfficeLayout.SUPERVISOR_DESK);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            setBpmnState(BpmnState.FINALIZADO);
        }
    }
}
