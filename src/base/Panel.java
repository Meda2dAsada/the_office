package base;

import javax.swing.JPanel;
import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Panel extends JPanel {

    private final Map<Integer, Sprite> sprites;
    private final Map<String, BufferedImage> imageCache;

    public Panel() {
        sprites = new HashMap<>();
        imageCache = new HashMap<>();

        setBackground(new Color(235, 235, 235));
    }

    public synchronized void setSprite(int index, Sprite sprite) {
        if (sprite == null) return;

        sprites.put(index, sprite);
        repaint();
    }

    public synchronized void addSprite(Sprite sprite) {
        if (sprite == null) return;

        sprites.put(sprite.index, sprite);
        repaint();
    }

    public synchronized void removeSprite(int index) {
        sprites.remove(index);
        repaint();
    }

    public synchronized void clearSprites() {
        sprites.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2d.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );

        // Creamos una copia para poder ordenar los sprites por su profundidad.
        ArrayList<Sprite> orderedSprites;

        synchronized (this) {
            orderedSprites = new ArrayList<>(sprites.values());
        }

        orderedSprites.sort(
                Comparator.comparingDouble(sprite -> sprite.order)
        );

        for (Sprite sprite : orderedSprites) {
            drawSprite(g2d, sprite);
        }

        g2d.dispose();
    }

    private void drawSprite(Graphics2D g2d, Sprite sprite) {
        BufferedImage image = getImage(sprite.sprite);

        if (image != null) {
            g2d.drawImage(
                    image,
                    Math.round(sprite.x),
                    Math.round(sprite.y),
                    null
            );
        } else {
            // Representación temporal cuando no se encuentra la imagen.
            g2d.setColor(Color.BLUE);
            g2d.fillRect(
                    Math.round(sprite.x),
                    Math.round(sprite.y),
                    50,
                    50
            );

            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.drawString(
                    String.valueOf(sprite.index),
                    Math.round(sprite.x) + 5,
                    Math.round(sprite.y) + 25
            );
        }
    }

    private BufferedImage getImage(String path) {
        if (path == null || path.isEmpty()) return null;

        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        try {
            BufferedImage image = ImageIO.read(new File(path));

            if (image != null) {
                imageCache.put(path, image);
            }

            return image;

        } catch (IOException e) {
            System.out.println("No se pudo cargar el sprite: " + path);
            return null;
        }
    }
}