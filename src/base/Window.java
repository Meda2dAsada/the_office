package base;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Window extends JFrame {

    private final Panel panel;

    public Window() {
        setTitle("Simulator");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        panel = new Panel();

        add(panel);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Actualiza o agrega un sprite según su índice.
    public void updateSprite(Sprite sprite) {
        if (sprite == null) return;

        SwingUtilities.invokeLater(() -> {
            panel.setSprite(sprite.index, sprite);
        });
    }

    // Recibe directamente el formato que envías por sockets.
    public void updateSprites(String spriteString) {
        Sprite sprite = Sprite.fromSubmissionFormat(spriteString);

        if (sprite == null) {
            System.out.println("Formato de sprite inválido: " + spriteString);
            return;
        }

        updateSprite(sprite);
    }

    // Agrega un sprite.
    public void addSprite(Sprite sprite) {
        if (sprite == null) return;

        SwingUtilities.invokeLater(() -> {
            panel.addSprite(sprite);
        });
    }

    // Elimina un sprite por su índice.
    public void removeSprite(int index) {
        SwingUtilities.invokeLater(() -> {
            panel.removeSprite(index);
        });
    }

    // Elimina todos los sprites.
    public void clearSprites() {
        SwingUtilities.invokeLater(() -> {
            panel.clearSprites();
        });
    }
}