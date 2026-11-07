package samples;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ImagePanelExample extends JPanel implements ActionListener {

    // Variables del Sprite (puedes cambiarlas por objetos Image de tus Sprites)
    private int spriteX = 50;
    private int spriteY = 150;
    private int velocidadX = 3;

    // Timer para controlar los FPS (60 FPS aprox = 16 milisegundos por frame)
    private Timer timer;

    public ImagePanelExample() {
        // Inicializa el timer para que llame al métod_o actionPerformed cada 16ms
        timer = new Timer(16, this);
        timer.start();
    }

    // 1. FUNCIÓN DE ACTUALIZACIÓN DE LÓGICA
    private void actualizarLogica() {
        // Mover el sprite horizontalmente
        spriteX += velocidadX;

        // Rebotar en los bordes del panel
        if (spriteX > getWidth() - 40 || spriteX < 0) {
            velocidadX = -velocidadX;
        }
    }

    // 2. FUNCIÓN DE RENDERIZADO (El Canva)
    @Override
    protected void paintComponent(Graphics g) {
        // super.paintComponent limpia el lienzo automáticamente evitando el "rastro"
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Activar antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // A) Dibujar el Escenario / Fondo
        g2d.setColor(new Color(34, 139, 34)); // Color verde pasto
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // B) Dibujar los Sprites Estáticos (ej. un obstáculo o casa)
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(300, 100, 60, 60);

        // C) Dibujar el Sprite Móvil
        // (Aquí usarías g2d.drawImage(miImagen, spriteX, spriteY, null);)
        g2d.setColor(Color.RED);
        g2d.fillOval(spriteX, spriteY, 40, 40);
    }

    // 3. EL BUCLE DE CONTROL (Llamado automáticamente por el Timer)
    @Override
    public void actionPerformed(ActionEvent e) {
        actualizarLogica(); // 1. Modifica las posiciones
        repaint();          // 2. Solicita a Java que vuelva a ejecutar paintComponent
    }

    public static void main(String[] args) {
        JFrame ventana = new JFrame("Escenario con Movimiento");
        ImagePanelExample juego = new ImagePanelExample();

        ventana.add(juego);
        ventana.setSize(600, 400);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }
}
