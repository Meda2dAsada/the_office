package base;

import java.io.*;
import java.net.Socket;

public class Client {
    private final String host;
    private final int port;
    private final ClientType clientType;
    private volatile String estadoConexion = "Sin conectar";

    public Client() {
        this("localhost", 5000, ClientType.DIRECTIONAL);
    }

    public Client(String host, int port, ClientType clientType) {
        this.host = host;
        this.port = port;
        this.clientType = clientType;
    }

    public String getEstadoConexion() {
        return estadoConexion;
    }

    public void connect() {
        while (!Thread.currentThread().isInterrupted()) {
            estadoConexion = "Conectando a " + host + ":" + port;

            try (Socket socket = new Socket(host, port);
                 BufferedReader input = new BufferedReader(
                         new InputStreamReader(socket.getInputStream()));
                 PrintWriter output =
                         new PrintWriter(socket.getOutputStream(), true)) {

                output.println(clientType);

                String mensaje;
                while ((mensaje = input.readLine()) != null) {
                    estadoConexion = mensaje;
                }

            } catch (IOException e) {
                System.out.println(
                        "Servidor no disponible: " + e.getMessage());
            }

            estadoConexion = "Desconectado. Reintento en 2 segundos";

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        new Client().connect();
    }
}