package base;

import java.net.Socket;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Client {
    private int port;
    private String host;
    private Socket socket;
    private ClientType clientType;

    public Client() {
        this("localhost", 5000, ClientType.DIRECTIONAL);
    }

    public Client(String host, int port, ClientType clientType) {
        this.host = host;
        this.port = port;
        this.clientType = clientType;
    }

    public void connect() {
        while (true) {
            try{
                socket = new Socket(host, port);

                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                output.println(clientType); // envia que tipo de cliente es

                while(true) { // aqui ya se puede actualizar la UI de la simulacion
                    if (clientType == ClientType.DIRECTIONAL) { // solo puede recibir informacion

                    } else { // puede recibir y enviar informacion

                    }

                }

                /* DEBUG
                String info = input.readLine();
                System.out.println(info + " informacion");
                Sprite s = Sprite.fromSubmissionFormat(info);
                System.out.println(s + " llego la info");
                */
            } catch (IOException e) {
                System.out.println("Trying to connect to " + host + ":" + port + ", in 2s");
            }
            try {Thread.sleep(2000);} catch (InterruptedException e) {}
        }

    }

    public static void main(String[] args) {
        Client client = new Client();
        client.connect();
    }

}
