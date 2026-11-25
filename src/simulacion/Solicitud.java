package simulacion;

import java.awt.*;

public class Solicitud {
    private final int numero;
    private final String tipoCliente;
    private boolean enFila, llamada, enPuesto, salida;
    private boolean terminada, registrada;
    private Point destinoB1;

    public Solicitud(int numero, String tipoCliente) {
        this.numero = numero;
        this.tipoCliente = tipoCliente;
    }

    public int getNumero() {
        return numero;
    }

    public String getTipoCliente() {
        return tipoCliente;
    }

    public synchronized boolean estaEnFila() {
        return enFila;
    }

    public synchronized boolean estaEnPuesto() {
        return enPuesto;
    }

    public synchronized boolean isRegistrada() {
        return registrada;
    }

    public synchronized Point getDestinoB1() {
        return new Point(destinoB1);
    }

    public synchronized void llegarFila() {
        enFila = true;
    }

    public synchronized void llamar(Point puesto) {
        destinoB1 = new Point(puesto.x - 50, puesto.y);
        enFila = false;
        llamada = true;
        notifyAll();
    }

    public synchronized boolean esperarLlamado(AgenteOficina cliente)
            throws InterruptedException {

        while (!llamada && !terminada) {
            cliente.cambiarEstado("Esperar turno en B3");
            wait();
        }
        return llamada && !terminada;
    }

    public synchronized void llegarPuesto() {
        enPuesto = true;
        notifyAll();
    }

    public synchronized boolean esperarLlegada(AgenteOficina recepcionista) throws InterruptedException {
        while (!enPuesto && !terminada) {
            recepcionista.cambiarEstado("Esperar llegada del cliente a B1");
            wait();
        }
        return enPuesto && !terminada;
    }

    public synchronized boolean finalizar(boolean registrada) {
        if (terminada) return false;
        this.registrada = registrada;
        terminada = true;
        notifyAll();
        return registrada;
    }

    public synchronized boolean esperarRespuesta(AgenteOficina cliente)
            throws InterruptedException {

        while (!terminada) {
            cliente.cambiarEstado("Recibir atencion en B1");
            wait();
        }
        return registrada;
    }

    public synchronized void salirPuesto() {
        enPuesto = false;
        salida = true;
        notifyAll();
    }

    public synchronized void esperarSalida(
            AgenteOficina recepcionista) throws InterruptedException {
        while (!salida) {
            recepcionista.cambiarEstado("Esperar salida del cliente de B1");
            wait();
        }
    }
}