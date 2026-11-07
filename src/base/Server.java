package base;

import java.net.Socket;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Server {
    private int port;
    //private ExecutorService pool;
    private ServerSocket serverSocket;
    private ArrayList<ClientManager> clients = new ArrayList<>();

    public Server() {
        this(5000);
    }

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        try {
            //pool = Executors.newCachedThreadPool();
            serverSocket = new ServerSocket(port);

            while (true) {
                // El hilo principal solo se encarga de aceptar clientes
                Socket clientSocket = serverSocket.accept();
                //BufferedReader entrada = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                //PrintWriter salida = new PrintWriter(clientSocket.getOutputStream(), true);

                addClient(new ClientManager(clientSocket)); // redirije los clientes a su respectivo manager
                Thread.sleep(1000);
            }
        }
        catch (Exception _) {}
    }

    private synchronized void addClient(ClientManager cm) {
        clients.add(cm);
        cm.start();
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

}


/*

    private boolean isMainServer;
    private static Window window;
    private HttpsServer server;

    public Server(int port, String ip) {
        setIsMainServer(ip == null);
    }


    @Override
    public void run() {
        while (true) {
            if (getIsMainServer()) {
                System.out.println("Im Main Server and im updating the window of my computer and sending info to the other computers");
            }else {
                System.out.println("Im and a client and im updating the window of my computer and receiving info from the main computer");
            }
        }
    }
public synchronized boolean connectTo() {
    setIsMainServer(false);
    return true;
}

public synchronized boolean disconnect() {
    setIsMainServer(true);
    return true;
}
public synchronized boolean update(Sprite sprite) {
    send(sprite);

    return true;
}

public synchronized void send(Sprite sprite) {
    String spriteString = Sprite.toSubmissionFormat(sprite);

    // aqui debe de enviar la informacion al otro servidor
}

public boolean getIsMainServer() {
    return isMainServer;
}

private void setIsMainServer(boolean isMainServer) {
    this.isMainServer = isMainServer;
}

            serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept();

            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);

            // Leer mensaje enviado por el cliente
            String mensajeRecibido = input.readLine();
            System.out.println("Mensaje del cliente: " + mensajeRecibido);

            // Enviar respuesta al cliente
            output.println("Hola cliente, mensaje recibido con éxito.");

            // Cerrar conexiones
            socketClient.close();
            System.out.println("Conexión cerrada.");

 */