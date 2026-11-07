package samples;

import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) {
        int puerto = 5000;
        try (ServerSocket servidorSocket = new ServerSocket(puerto)) {
            System.out.println("Servidor iniciado en el puerto " + puerto + ". Esperando cliente...");

            // Bloquea hasta que un cliente se conecte
            Socket socketCliente = servidorSocket.accept();
            System.out.println("¡Cliente conectado desde " + socketCliente.getInetAddress() + "!");

            // Canales de entrada y salida de datos
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
            PrintWriter salida = new PrintWriter(socketCliente.getOutputStream(), true);

            // Leer mensaje enviado por el cliente
            String mensajeRecibido = entrada.readLine();
            System.out.println("Mensaje del cliente: " + mensajeRecibido);

            // Enviar respuesta al cliente
            salida.println("Hola cliente, mensaje recibido con éxito.");

            // Cerrar conexiones
            socketCliente.close();
            System.out.println("Conexión cerrada.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
