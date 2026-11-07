package base;
import javax.swing.JFrame;
import java.util.ArrayList;

public class Window extends JFrame {
    public static final ArrayList<Sprite> GLOBAL_AGENTS = new ArrayList<>();
    public static final ArrayList<Sprite> GLOBAL_BACKGROUND = new ArrayList<>();
    private Panel panel;

    public Window() {
        setTitle("Simulator");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        setResizable(false);
        //setLocationRelativeTo(null);
        panel = new Panel(GLOBAL_AGENTS);
        add(panel);
        setVisible(true);
    }


    public synchronized void updateSprites(String spriteString) {
        Sprite sprite = Sprite.fromSubmissionFormat(spriteString);
        panel.setSprite(sprite.index, sprite);
    }

    public synchronized void repaint() {}

}
