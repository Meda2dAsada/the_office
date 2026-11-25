package base;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientManager extends Thread {
    private final Socket clientSocket;
    private final ArrayList<Sprite> sprites = new ArrayList<>();

    public ClientManager(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (Socket socket = clientSocket;
             BufferedReader input = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter output =
                     new PrintWriter(socket.getOutputStream(), true)) {

            ClientType tipo = ClientType.fromValue(input.readLine());

            if (tipo == null) {
                output.println("Tipo de cliente no valido");
                return;
            }

            output.println("Conectado al servidor de Iker");

            String mensaje;
            while ((mensaje = input.readLine()) != null) {
                if (tipo == ClientType.BIDIRECTIONAL) {
                    update(mensaje);
                }
            }

        } catch (IOException e) {
            System.out.println(
                    "Conexion finalizada: " + e.getMessage());
        }
    }

    private synchronized void update(String mensaje) {
        try {
            Sprite sprite = Sprite.fromSubmissionFormat(mensaje);

            if (sprite != null) {
                sprites.add(sprite);
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Formato de sprite no valido");
        }
    }
}