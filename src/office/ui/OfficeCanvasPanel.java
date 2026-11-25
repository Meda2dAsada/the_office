package office.ui;

import office.agents.OfficeAgent;
import office.model.AgentType;
import office.model.BpmnState;
import office.model.OfficeLayout;
import office.sync.OfficeContext;
import office.sync.TrackedResource;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

/**
 * Representacion grafica 2D animada del plano de la oficina "The Office WTC".
 * Muestra las zonas/buferes (B1..B7) con su ocupacion en vivo y los agentes moviendose segun su estado BPMN.
 */
public class OfficeCanvasPanel extends JPanel {
    private final OfficeContext context;

    public OfficeCanvasPanel(OfficeContext context) {
        this.context = context;
        setPreferredSize(new Dimension(OfficeLayout.CANVAS_WIDTH, OfficeLayout.CANVAS_HEIGHT));
        setBackground(new Color(244, 246, 250));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double sx = (double) getWidth() / OfficeLayout.CANVAS_WIDTH;
        double sy = (double) getHeight() / OfficeLayout.CANVAS_HEIGHT;
        g2.scale(sx, sy);

        drawFloorPlan(g2);
        drawZones(g2);
        drawAgents(g2);

        g2.dispose();
    }

    private void drawFloorPlan(Graphics2D g2) {
        // Contorno de oficina
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(14, 14, OfficeLayout.CANVAS_WIDTH - 28, OfficeLayout.CANVAS_HEIGHT - 28, 16, 16);
        g2.setColor(new Color(180, 190, 205));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(14, 14, OfficeLayout.CANVAS_WIDTH - 28, OfficeLayout.CANVAS_HEIGHT - 28, 16, 16);

        // Titulo del plano
        g2.setColor(new Color(45, 55, 72));
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        String siteLabel = context.getConfig().getDistributedMode().toString();
        g2.drawString("PLANO 2D - THE OFFICE WTC (" + siteLabel + ")", 28, 36);

        // Flechas de flujo basico
        g2.setColor(new Color(210, 218, 228));
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{6, 6}, 0));
        drawLink(g2, OfficeLayout.ENTRANCE, OfficeLayout.B3_QUEUE);
        drawLink(g2, OfficeLayout.B3_QUEUE, OfficeLayout.B1_RECEPTION);
        drawLink(g2, OfficeLayout.RECEPTIONIST_DESK, OfficeLayout.B4_TRAY);
        drawLink(g2, OfficeLayout.B4_TRAY, OfficeLayout.SELLER_DESK);
        drawLink(g2, OfficeLayout.SELLER_DESK, OfficeLayout.B2_PRINTER);
        drawLink(g2, OfficeLayout.SELLER_DESK, OfficeLayout.B5_TRAY);
        drawLink(g2, OfficeLayout.B5_TRAY, OfficeLayout.SUPERVISOR_DESK);
        drawLink(g2, OfficeLayout.SUPERVISOR_DESK, OfficeLayout.B6_TRAY);
        drawLink(g2, OfficeLayout.B6_TRAY, OfficeLayout.ADMIN_DESK);
        drawLink(g2, OfficeLayout.CLIENT_WAIT_AREA, OfficeLayout.EXIT);
        g2.setStroke(new BasicStroke(1.5f));
    }

    private void drawLink(Graphics2D g2, Point a, Point b) {
        g2.drawLine(a.x, a.y, b.x, b.y);
    }

    private void drawZones(Graphics2D g2) {
        drawStationBox(g2, OfficeLayout.ENTRANCE, 76, 48, "Entrada", new Color(226, 232, 240));
        drawResourceBox(g2, OfficeLayout.B3_QUEUE, 108, 66, "B3 Fila", context.getB3ReceptionQueue(), new Color(254, 243, 199));
        drawResourceBox(g2, OfficeLayout.B1_RECEPTION, 118, 66, "B1 Recepcion", context.getB1ReceptionDesk(), new Color(219, 234, 254));
        drawStationBox(g2, OfficeLayout.CLIENT_WAIT_AREA, 128, 56, "Espera Resultado", new Color(241, 245, 249));
        drawStationBox(g2, OfficeLayout.EXIT, 76, 48, "Salida", new Color(220, 252, 231));

        drawResourceBox(g2, OfficeLayout.B4_TRAY, 116, 62, "B4 Solicitudes", context.getB4RequestTray(), new Color(224, 231, 255));
        drawStationBox(g2, OfficeLayout.SELLER_DESK, 118, 56, "Esc. Vendedor", new Color(237, 233, 254));
        drawResourceBox(g2, OfficeLayout.B2_PRINTER, 112, 66, "B2 Impresora", context.getB2Printer(), new Color(254, 226, 226));
        drawResourceBox(g2, OfficeLayout.B5_TRAY, 116, 62, "B5 Revision", context.getB5ReviewTray(), new Color(224, 231, 255));
        drawStationBox(g2, OfficeLayout.SUPERVISOR_DESK, 122, 56, "Esc. Supervisor", new Color(255, 237, 213));
        drawResourceBox(g2, OfficeLayout.B6_TRAY, 116, 62, "B6 Resultados", context.getB6ResultTray(), new Color(224, 231, 255));
        drawStationBox(g2, OfficeLayout.ADMIN_DESK, 122, 56, "Esc. Admin", new Color(209, 250, 229));
        drawResourceBox(g2, OfficeLayout.B7_REST, 120, 70, "B7 Descanso", context.getB7RestZone(), new Color(243, 232, 255));
        drawStationBox(g2, OfficeLayout.BRANCH_GATE, 112, 56, "Acceso Sede", new Color(207, 250, 254));
    }

    private void drawStationBox(Graphics2D g2, Point center, int w, int h, String label, Color fill) {
        int rx = center.x - w / 2;
        int ry = center.y - h / 2;
        g2.setColor(fill);
        g2.fillRoundRect(rx, ry, w, h, 10, 10);
        g2.setColor(new Color(148, 163, 184));
        g2.drawRoundRect(rx, ry, w, h, 10, 10);

        g2.setColor(new Color(30, 41, 59));
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.drawString(label, rx + 8, ry + 18);
    }

    private void drawResourceBox(Graphics2D g2, Point center, int w, int h, String label, TrackedResource res, Color fill) {
        drawStationBox(g2, center, w, h, label, fill);
        int rx = center.x - w / 2;
        int ry = center.y - h / 2;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(new Color(51, 65, 85));
        String occ = "Uso: " + res.getCurrentCount() + "/" + res.getConfig().getM() + " (max " + res.getConfig().getN() + ")";
        String wait = "Esperando: " + res.getWaitingCount();
        g2.drawString(occ, rx + 8, ry + 34);
        g2.drawString(wait, rx + 8, ry + 48);
    }

    private void drawAgents(Graphics2D g2) {
        for (OfficeAgent agent : context.getAgents()) {
            if (agent.getBpmnState() == BpmnState.FINALIZADO && agent.getAgentType() == AgentType.CLIENTE) {
                continue;
            }
            int ax = Math.round(agent.getPosX());
            int ay = Math.round(agent.getPosY());

            Color color = colorFor(agent.getAgentType());
            g2.setColor(color);
            g2.fillOval(ax - 13, ay - 13, 26, 26);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(ax - 13, ay - 13, 26, 26);

            // ID dentro del token
            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            String shortId = agent.getAgentId().length() > 5 ? agent.getAgentId().substring(0, 5) : agent.getAgentId();
            g2.drawString(shortId, ax - 10, ay + 3);

            // Etiqueta de estado BPMN debajo
            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            String stateText = agent.getBpmnState().getLabel();
            if (stateText.length() > 24) {
                stateText = stateText.substring(0, 22) + "..";
            }
            int tw = g2.getFontMetrics().stringWidth(stateText);
            g2.setColor(new Color(255, 255, 255, 220));
            g2.fillRoundRect(ax - tw / 2 - 3, ay + 15, tw + 6, 13, 6, 6);
            g2.setColor(new Color(15, 23, 42));
            g2.drawString(stateText, ax - tw / 2, ay + 25);
        }
    }

    private Color colorFor(AgentType type) {
        return switch (type) {
            case CLIENTE -> new Color(37, 99, 235);
            case RECEPCIONISTA -> new Color(219, 39, 119);
            case VENDEDOR -> new Color(124, 58, 237);
            case SUPERVISOR -> new Color(234, 88, 12);
            case ADMINISTRADOR -> new Color(5, 150, 105);
            case SUCURSAL -> new Color(14, 116, 144);
        };
    }
}
