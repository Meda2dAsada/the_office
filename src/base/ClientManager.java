package base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

// es una "extension" de la clase Server que le ayuda a manejar multiples conexiones de clientes
public class ClientManager extends Thread {
    private Socket clientSocket;
    private ClientType clientType;
    private ArrayList<Sprite> sprites;

    private BufferedReader input;
    private PrintWriter output;

    public ClientManager(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            // debe de tener un tipo de cliente valido cuando se hace client.connect()
            while ((clientType = ClientType.fromValue(input.readLine())) == null) {}

            if(clientType == ClientType.DIRECTIONAL) { // / solo puede recibir informacion
                //Sprite current = new Sprite("prueba/de/enviar/informacion", 10, 10, 2, 10);
                //output.println(Sprite.toSubmissionFormat(current));

                update(input.readLine());
            }else { // puede recibir y enviar informacion

            }

        } catch (IOException e) {}
    }

    private synchronized void update(String spriteFromSubmission) {
        Sprite sprite = Sprite.fromSubmissionFormat(spriteFromSubmission);
        if(sprite == null) return;

        sprites.add(sprite);
        System.out.println("New sprite: " + sprite);
    }
}
