package office.agents;

import office.model.AgentType;
import office.model.BpmnState;
import office.model.OfficeLayout;
import office.model.Order;
import office.sync.OfficeContext;

import java.awt.Point;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Agente Cliente (BPMN 01): llega a la oficina, espera fila B3, presenta su pedido en B1,
 * espera el resultado (comprobante o rechazo) y sale de la oficina.
 */
public class ClientAgent extends OfficeAgent {
    private final Order.ClientProfile profile;

    public ClientAgent(OfficeContext context, String agentId, String siteName) {
        super(context, agentId, AgentType.CLIENTE, siteName, OfficeLayout.ENTRANCE);
        Order.ClientProfile[] profiles = Order.ClientProfile.values();
        this.profile = profiles[ThreadLocalRandom.current().nextInt(profiles.length)];
    }

    @Override
    public void run() {
        try {
            if (!shouldKeepRunning()) return;

            // Generar pedido aleatorio segun perfil (algunos superan 50 unidades para ver rechazos)
            ThreadLocalRandom rnd = ThreadLocalRandom.current();
            int hojas = rnd.nextInt(5, 36);
            int cuadernos = rnd.nextInt(2, 18);
            int carpetas = rnd.nextInt(1, 14);
            Order order = new Order(agentId, profile, hojas, cuadernos, carpetas);
            setDetail(profile.getLabel() + " (" + order.getTotalUnits() + " uds)");

            // 1. Caminar a la entrada y esperar cupo en fila B3
            setBpmnState(BpmnState.LLEGANDO);
            moveTo(OfficeLayout.ENTRANCE.x + 25, OfficeLayout.ENTRANCE.y);
            if (!shouldKeepRunning()) return;

            setBpmnState(BpmnState.ESPERANDO_FILA_B3);
            context.getB3ReceptionQueue().enter();
            boolean inB3 = true;
            boolean inB1 = false;
            try {
                if (!shouldKeepRunning()) return;
                setBpmnState(BpmnState.EN_FILA_B3);
                moveTo(new Point(OfficeLayout.B3_QUEUE.x, OfficeLayout.B3_QUEUE.y + rnd.nextInt(-18, 19)));

                // 2. Reservar puesto de recepcion B1 y liberar B3
                setBpmnState(BpmnState.CAMINANDO_MOSTRADOR);
                context.getB1ReceptionDesk().enter();
                inB1 = true;
                context.getB3ReceptionQueue().exit();
                inB3 = false;

                if (!shouldKeepRunning()) return;
                setBpmnState(BpmnState.EN_RECEPCION_B1);
                moveTo(OfficeLayout.B1_RECEPTION);
                context.log(agentId + " (" + profile.getLabel() + ") presenta solicitud de " + order.getTotalUnits() + " uds en B1.");

                // Entrega solicitud a Recepcionista y espera numero de pedido
                context.getDeskHandoff().submitAndWaitRegistration(order);
            } finally {
                if (inB1) {
                    context.getB1ReceptionDesk().exit();
                }
                if (inB3) {
                    context.getB3ReceptionQueue().exit();
                }
            }

            if (!shouldKeepRunning()) return;
            if (order.getId() > 0) {
                setCurrentOrderId("#" + order.getId());
            }

            // 3. Liberado B1, caminar a espera de resultado
            setBpmnState(BpmnState.ESPERANDO_RESULTADO);
            setDetail("Esperando respuesta pedido #" + order.getId());
            moveTo(new Point(
                    OfficeLayout.CLIENT_WAIT_AREA.x + rnd.nextInt(-30, 31),
                    OfficeLayout.CLIENT_WAIT_AREA.y + rnd.nextInt(-15, 16)
            ));

            order.awaitCompletion();
            if (!shouldKeepRunning()) return;

            // 4. Recibir comprobante o motivo de rechazo y salir
            setBpmnState(BpmnState.RECIBIENDO_RESPUESTA);
            if (order.isApproved()) {
                setDetail("Aprobado ($" + String.format("%.2f", order.getTotalAmount()) + ")");
                context.log(agentId + " recibio comprobante de pedido #" + order.getId() + ".");
            } else {
                setDetail("Rechazado: " + order.getReason());
                context.log(agentId + " recibio rechazo pedido #" + order.getId() + ": " + order.getReason());
            }
            workSleep(450);

            setBpmnState(BpmnState.SALIENDO);
            moveTo(OfficeLayout.EXIT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            setBpmnState(BpmnState.FINALIZADO);
        }
    }
}
