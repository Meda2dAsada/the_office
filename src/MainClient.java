
import base.Client;
import base.ClientType;
import base.Window;

import javax.swing.SwingUtilities;

public class MainClient {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Window window = new Window();

            Client client = new Client(
                    "localhost",
                    5000,
                    ClientType.DIRECTIONAL,
                    window
            );

            new Thread(client::connect).start();
        });
    }
}