package base;

import java.net.Socket;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Client {

    private final int port;
    private final String host;
    private final ClientType clientType;
    private final Window window;

    private Socket socket;
    private PrintWriter output;

    public Client(Window window) {
        this("localhost", 5000, ClientType.DIRECTIONAL, window);
    }

    public Client(String host, int port, ClientType clientType, Window window) {
        this.host = host;
        this.port = port;
        this.clientType = clientType;
        this.window = window;

    }

    public void connect() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                socket = new Socket(host, port);

                BufferedReader input = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );

                output = new PrintWriter(socket.getOutputStream(), true);

                output.println(clientType);


                // Prueba de envío de un sprite
                Sprite sprite = new Sprite(
                        "sprites/person.png",
                        100,
                        100,
                        1,
                        0
                );

                // Actualiza la interfaz del propio cliente
                window.updateSprite(sprite);

                // Envía el sprite al servidor
                sendSprite(sprite);

                System.out.println("Connected to " + host + ":" + port);

                String message;

                while ((message = input.readLine()) != null) {
                    window.updateSprites(message);
                }

            } catch (IOException e) {
                System.out.println(
                        "Trying to connect to " + host + ":" + port + ", in 2s"
                );

            } finally {
                try {
                    if (socket != null) socket.close();
                } catch (IOException e) {
                    // No hay nada que hacer.
                }
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // Envía un sprite al servidor.
    public synchronized void sendSprite(Sprite sprite) {
        if (output == null || sprite == null) return;

        output.println(Sprite.toSubmissionFormat(sprite));
    }
}