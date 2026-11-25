import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class OficinaTheOffice extends JPanel {
    private static final String RUTA_PLANO = "Planos oficina de The office/Plano1.jpg";
    private static final int PASO_CUADRICULA = 100;
    private final BufferedImage plano;
    private boolean mostrarCuadricula;

    public OficinaTheOffice(BufferedImage plano) {
        this.plano = plano;
        setPreferredSize(new Dimension(1280, 1280 * plano.getHeight() / plano.getWidth()));
        setBackground(new Color(232, 237, 234));
    }

    private static BufferedImage cargarPlano() throws IOException {
        InputStream recurso = OficinaTheOffice.class.getResourceAsStream("/" + RUTA_PLANO);
        if (recurso == null) recurso = new FileInputStream(RUTA_PLANO);
        try (InputStream entrada = recurso) {
            BufferedImage imagen = ImageIO.read(entrada);
            if (imagen == null) throw new IOException("Formato de imagen no compatible.");
            return imagen;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            double escala = Math.min(getWidth() / (double) plano.getWidth(),
                    getHeight() / (double) plano.getHeight());
            g2.translate((getWidth() - plano.getWidth() * escala) / 2,
                    (getHeight() - plano.getHeight() * escala) / 2);
            g2.scale(escala, escala);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            g2.drawImage(plano, 0, 0, null);
            if (mostrarCuadricula) dibujarCuadricula(g2);
        } finally {
            g2.dispose();
        }
    }

    private void dibujarCuadricula(Graphics2D g) {
        g.setColor(new Color(30, 95, 130, 100));
        g.setStroke(new BasicStroke(1));
        for (int x = 0; x < plano.getWidth(); x += PASO_CUADRICULA)
            g.drawLine(x, 0, x, plano.getHeight());
        for (int y = 0; y < plano.getHeight(); y += PASO_CUADRICULA)
            g.drawLine(0, y, plano.getWidth(), y);
    }

    private static void crearVentana(OficinaTheOffice oficina) {
        JFrame ventana = new JFrame("Dunder Mifflin | Oficina 2D");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setLayout(new BorderLayout());

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 12));
        JLabel titulo = new JLabel("DUNDER MIFFLIN  /  PLANO DE OFICINA");
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        barra.add(titulo);

        JCheckBox cuadricula = new JCheckBox("Cuadr\u00edcula de 100 p\u00edxeles");
        cuadricula.addActionListener(e -> {
            oficina.mostrarCuadricula = cuadricula.isSelected();
            oficina.repaint();
        });
        barra.add(cuadricula);

        JLabel pie = new JLabel("Vista superior | " + RUTA_PLANO);
        pie.setBorder(BorderFactory.createEmptyBorder(8, 18, 10, 18));
        ventana.add(barra, BorderLayout.NORTH);
        ventana.add(oficina, BorderLayout.CENTER);
        ventana.add(pie, BorderLayout.SOUTH);
        ventana.pack();
        ventana.setMinimumSize(new Dimension(850, 500));
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            BufferedImage imagen = cargarPlano();
            SwingUtilities.invokeLater(() -> crearVentana(new OficinaTheOffice(imagen)));
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "No se pudo cargar " + RUTA_PLANO + "\n"
                            + "Conserva la carpeta de planos dentro del proyecto.\n"
                            + e.getMessage(),
                    "Error al cargar la oficina", JOptionPane.ERROR_MESSAGE));
        }
    }
}
