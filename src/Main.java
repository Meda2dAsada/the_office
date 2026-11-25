import base.Client;
import base.ClientType;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        Client conexion = new Client(
                "localhost", 5000, ClientType.BIDIRECTIONAL);

        Thread hiloConexion = new Thread(
                conexion::connect, "Conexion-servidor");

        hiloConexion.setDaemon(true);
        hiloConexion.start();

        SwingUtilities.invokeLater(
                () -> new VentanaSimulador(conexion));
    }
}