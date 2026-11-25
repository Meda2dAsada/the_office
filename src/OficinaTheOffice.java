import simulacion.*;
import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class OficinaTheOffice extends JPanel {
    private static final int ANCHO = 1000;
    private static final int ALTO = 500;
    private List<AgenteOficina> agentes = Collections.emptyList();
    private boolean mostrarCuadricula;

    public OficinaTheOffice() {
        setPreferredSize(new Dimension(ANCHO, ALTO));
        setBackground(Color.WHITE);
    }

    public static OficinaTheOffice crearPanel() {
        return new OficinaTheOffice();
    }

    public void setSimulacion(Simulacion simulacion) {
        agentes = simulacion.getAgentes();
        repaint();
    }

    public void setMostrarCuadricula(boolean visible) {
        mostrarCuadricula = visible;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D dibujo = (Graphics2D) g.create();

        try {
            double escala = Math.min(getWidth() / (double) ANCHO, getHeight() / (double) ALTO);
            dibujo.translate((getWidth() - ANCHO * escala) / 2, (getHeight() - ALTO * escala) / 2);

            dibujo.scale(escala, escala);

            dibujo.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (mostrarCuadricula) {
                dibujo.setColor(new Color(230, 230, 230));

                for (int x = 0; x <= ANCHO; x += 100) {
                    dibujo.drawLine(x, 0, x, ALTO);
                }

                for (int y = 0; y <= ALTO; y += 100) {
                    dibujo.drawLine(0, y, ANCHO, y);
                }
            }

            for (AgenteOficina agente : agentes) {
                if (!agente.isVisible()) continue;

                Point posicion = agente.getPosicion();

                Color color = agente.getTipo().equals("Recepcionista") ? new Color(220, 55, 55) : new Color(40, 110, 230);

                dibujo.setColor(color);
                dibujo.fillOval(posicion.x - 12, posicion.y - 12, 24, 24);

                dibujo.setColor(color.darker());
                dibujo.drawOval(posicion.x - 12, posicion.y - 12, 24, 24);
            }

        } finally {
            dibujo.dispose();
        }
    }
}