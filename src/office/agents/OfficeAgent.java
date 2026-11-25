package office.agents;

import office.model.AgentType;
import office.model.BpmnState;
import office.model.OfficeLayout;
import office.sync.OfficeContext;

import java.awt.Point;

/**
 * Clase base para todos los agentes concurrentes del simulador.
 * Cada agente corre en su propio hilo Java y reporta su estado BPMN y posicion 2D.
 */
public abstract class OfficeAgent extends Thread {
    protected final OfficeContext context;
    protected final String agentId;
    protected final AgentType agentType;
    protected String siteName;

    private volatile BpmnState bpmnState = BpmnState.INICIO;
    private volatile float x;
    private volatile float y;
    private volatile String currentOrderId = "-";
    private volatile String detail = "";
    private volatile boolean stopRequested = false;

    protected OfficeAgent(OfficeContext context, String agentId, AgentType agentType, String siteName, Point startPos) {
        super("Agent-" + agentId);
        this.context = context;
        this.agentId = agentId;
        this.agentType = agentType;
        this.siteName = siteName;
        this.x = startPos.x;
        this.y = startPos.y;
    }

    public String getAgentId() { return agentId; }
    public AgentType getAgentType() { return agentType; }
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }

    public BpmnState getBpmnState() { return bpmnState; }
    protected void setBpmnState(BpmnState state) { this.bpmnState = state; }

    public float getPosX() { return x; }
    public float getPosY() { return y; }
    protected void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public String getCurrentOrderId() { return currentOrderId; }
    protected void setCurrentOrderId(String currentOrderId) { this.currentOrderId = currentOrderId; }

    public String getDetail() { return detail; }
    protected void setDetail(String detail) { this.detail = detail; }

    public void requestStop() {
        this.stopRequested = true;
    }

    protected boolean shouldKeepRunning() {
        return context.isRunning() && !stopRequested && !isInterrupted();
    }

    /**
     * Anima suavemente al agente hacia un punto destino del plano de la oficina.
     */
    protected void moveTo(Point target) throws InterruptedException {
        moveTo(target.x, target.y);
    }

    protected void moveTo(float targetX, float targetY) throws InterruptedException {
        int steps = 12;
        float startX = this.x;
        float startY = this.y;
        int stepMs = context.getConfig().getAnimationStepMs();
        for (int i = 1; i <= steps; i++) {
            if (!shouldKeepRunning()) return;
            context.checkPause();
            float t = (float) i / steps;
            this.x = startX + (targetX - startX) * t;
            this.y = startY + (targetY - startY) * t;
            Thread.sleep(stepMs);
        }
        this.x = targetX;
        this.y = targetY;
    }

    protected void workSleep(int totalMs) throws InterruptedException {
        int chunk = 100;
        int elapsed = 0;
        while (elapsed < totalMs && shouldKeepRunning()) {
            context.checkPause();
            int sleepNow = Math.min(chunk, totalMs - elapsed);
            Thread.sleep(sleepNow);
            elapsed += sleepNow;
        }
    }

    /**
     * Descanso compartido en B7 controlado por semaforo (capacidad m de n).
     */
    protected void takeRestInB7(Point returnDesk) throws InterruptedException {
        setBpmnState(BpmnState.ESPERANDO_DESCANSO_B7);
        setDetail("Esperando cupo en descanso B7");
        moveTo(OfficeLayout.B7_REST.x - 45, OfficeLayout.B7_REST.y);

        context.getB7RestZone().enter();
        try {
            if (!shouldKeepRunning()) return;
            setBpmnState(BpmnState.EN_DESCANSO_B7);
            setDetail("Descansando en B7");
            moveTo(OfficeLayout.B7_REST);
            context.log(agentId + " entro a zona de descanso B7.");
            workSleep(context.getConfig().getRestDurationMs());
        } finally {
            context.getB7RestZone().exit();
        }
        moveTo(returnDesk);
    }
}
