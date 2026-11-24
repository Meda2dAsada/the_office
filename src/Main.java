
import base.Server;
import base.Window;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Window window = new Window();

            Server server = new Server(5000, window);

            new Thread(server::start).start();
        });
    }
}