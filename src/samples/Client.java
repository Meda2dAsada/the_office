package samples;

import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws InterruptedException {
        String direccionServidor = "127.0.0.1";
        int puerto = 5000;
        boolean noError = true;

        while(noError){
            try (Socket socket = new Socket(direccionServidor, puerto)) { //
                noError = false;
                // Canales de salida y entrada de datos
                PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Enviar mensaje al servidor
                String mensaje = "Hola servidor, salude desde el cliente de Java.";
                salida.println(mensaje);

                // Leer respuesta del servidor
                String respuesta = entrada.readLine();
                System.out.println("Respuesta del servidor: " + respuesta);

            } catch (IOException e) {
                System.out.println("Intentado conectar en 1s");
            }
            Thread.sleep(1000);
        }


    }
}
