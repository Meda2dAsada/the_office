package base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientManager extends Thread {

    private final Socket clientSocket;
    private final Window window;
    private final Server server;

    private ClientType clientType;

    private BufferedReader input;
    private PrintWriter output;

    public ClientManager(Socket clientSocket, Window window, Server server) {
        this.clientSocket = clientSocket;
        this.window = window;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            input = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream())
            );

            output = new PrintWriter(clientSocket.getOutputStream(), true);

            String type = input.readLine();

            clientType = ClientType.fromValue(type);

            if (clientType == null) {
                return;
            }

            String message;

            while ((message = input.readLine()) != null) {
                Sprite sprite = Sprite.fromSubmissionFormat(message);

                if (sprite == null) continue;

                // Actualiza la ventana del servidor.
                window.updateSprite(sprite);

                // Distribuye el cambio a los demás clientes.
                server.broadcastSprite(message, this);
            }

        } catch (IOException e) {
            System.out.println("Client disconnected.");

        } finally {
            server.removeClient(this);

            try {
                clientSocket.close();
            } catch (IOException e) {
                // No hay nada que hacer.
            }
        }
    }

    public synchronized void send(String message) {
        if (output != null) {
            output.println(message);
        }
    }
}