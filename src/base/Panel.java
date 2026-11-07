package base;

import javax.swing.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Panel extends JPanel implements ActionListener {
    private Timer timer;
    private ArrayList<Sprite> sprites;

    public Panel(ArrayList<Sprite> sprites) {
        timer = new Timer(16, this);
        timer.start();

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // convertir Graphics a Graphics2D para usar funciones avanzadas
        Graphics2D g2d = (Graphics2D) g;
        // activar suavizado de bordes (antialiasing) para mejor calidad
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //  cambiar color y dibujar un rectángulo relleno
        g2d.setColor(Color.BLUE);
        g2d.fillRect(50, 50, 100, 80); // (x, y, ancho, alto)

        // 4. Cambiar grosor de línea con BasicStroke
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(4)); // Grosor de 4 píxeles
        g2d.drawOval(200, 50, 80, 80);     // (x, y, ancho, alto)

        // 5. Dibujar texto
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Hola Graphics2D", 50, 180);
    }

    public synchronized void setSprite(int index, Sprite sprite) {}


    public static void main(String[] args) {
        JFrame ventana = new JFrame("Ejemplo Java Graphics2D");
        Panel panel = new Panel(null);

        ventana.add(panel);
        ventana.setSize(400, 300);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //updateAgents();
        repaint();

    }
}

