package simulacion;

import java.util.LinkedList;
import java.util.Queue;

public class Recepcion {
    private final Queue<Solicitud> fila = new LinkedList<>();
    private final Solicitud[] lugares;
    private Solicitud puesto;
    private int esperandoEntrada, registradas;

    public Recepcion(int capacidadFila) {
        if (capacidadFila < 1 || capacidadFila > 3) {
            throw new IllegalArgumentException("B3 admite de 1 a 3 clientes");
        }
        lugares = new Solicitud[capacidadFila];
    }

    private int lugarLibre() {
        for (int i = 0; i < lugares.length; i++) {
            if (lugares[i] == null) return i;
        }
        return -1;
    }

    private void liberarLugar(Solicitud solicitud) {
        for (int i = 0; i < lugares.length; i++) {
            if (lugares[i] == solicitud) {
                lugares[i] = null;
            }
        }
    }

    public synchronized int entrar(
            Solicitud solicitud, AgenteOficina cliente)
            throws InterruptedException {

        while (lugarLibre() == -1) {
            cliente.cambiarEstado("Esperar fuera: B3 llena");
            esperandoEntrada++;

            try {
                wait();
            } finally {
                esperandoEntrada--;
            }
        }

        int lugar = lugarLibre();
        lugares[lugar] = solicitud;
        fila.offer(solicitud);
        notifyAll();
        return lugar;
    }

    public synchronized void llegarFila(Solicitud solicitud) {
        solicitud.llegarFila();
        notifyAll();
    }

    public synchronized Solicitud siguiente(
            AgenteOficina recepcionista) throws InterruptedException {

        while (puesto != null || fila.isEmpty() || !fila.peek().estaEnFila()) {
            recepcionista.cambiarEstado(puesto != null
                    ? "Esperar B1 libre"
                    : "Esperar cliente en B3");
            wait();
        }
        puesto = fila.poll();
        liberarLugar(puesto);
        puesto.llamar(recepcionista.getPosicion());
        notifyAll();
        return puesto;
    }

    public synchronized void registrar(Solicitud solicitud) {
        if (puesto == solicitud && solicitud.finalizar(true)) {
            registradas++;
        }
    }

    public synchronized void liberarPuesto(Solicitud solicitud) {
        if (puesto == solicitud) {
            puesto = null;
        }
        notifyAll();
    }

    public synchronized void retirar(Solicitud solicitud) {
        fila.remove(solicitud);
        liberarLugar(solicitud);
        solicitud.finalizar(false);
        solicitud.salirPuesto();
        notifyAll();
    }

    public synchronized int[] getDatos() {
        int dentroFila = 0;

        for (Solicitud solicitud : fila) {
            if (solicitud.estaEnFila()) {
                dentroFila++;
            }
        }

        return new int[]{
                puesto != null && puesto.estaEnPuesto() ? 1 : 0, dentroFila, esperandoEntrada, registradas
        };
    }
}