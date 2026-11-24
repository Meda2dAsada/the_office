package base;

import java.net.Socket;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class Server {

    private final int port;
    private final Window window;

    private ServerSocket serverSocket;
    private final ArrayList<ClientManager> clients;

    public Server(Window window) {
        this(5000, window);
    }

    public Server(int port, Window window) {
        this.port = port;
        this.window = window;
        this.clients = new ArrayList<>();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);

            System.out.println("Server running on port " + port);

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();

                ClientManager manager = new ClientManager(
                        clientSocket,
                        window,
                        this
                );

                addClient(manager);
            }

        } catch (IOException e) {
            System.out.println("Server stopped: " + e.getMessage());
        }
    }

    private synchronized void addClient(ClientManager manager) {
        clients.add(manager);
        manager.start();
    }

    public synchronized void removeClient(ClientManager manager) {
        clients.remove(manager);
    }

    // Envía un sprite a todos los clientes excepto al que lo originó.
    public synchronized void broadcastSprite(
            String message,
            ClientManager sender
    ) {
        for (ClientManager client : clients) {
            if (client != sender) {
                client.send(message);
            }
        }
    }

    // Envía un sprite a todos los clientes.
    public synchronized void broadcastSprite(String message) {
        broadcastSprite(message, null);
    }

    // El servidor también puede generar actualizaciones.
    public void updateSprite(Sprite sprite) {
        if (sprite == null) return;

        window.updateSprite(sprite);

        broadcastSprite(Sprite.toSubmissionFormat(sprite));
    }
}