package simulacion;

import java.util.*;

public class Simulacion {
    private final Parametros parametros;
    private final Recepcion recepcion;
    private final List<AgenteOficina> agentes = new ArrayList<>();
    private volatile boolean activa;
    private boolean iniciada;

    public Simulacion(Parametros parametros) {
        this.parametros = parametros;
        recepcion = new Recepcion(parametros.getCapacidad(2));

        for (int i = 1; i <= parametros.getRecepcionistas(); i++) {
            agentes.add(new Recepcionista(i, parametros, recepcion));
        }

        for (int i = 1; i <= parametros.getClientes(); i++) {
            agentes.add(new ClienteOficina(i, parametros, recepcion));
        }
    }

    public void iniciar() {
        if (iniciada) {
            throw new IllegalStateException("Esta simulacion ya fue iniciada");
        }

        iniciada = true;
        activa = true;

        for (AgenteOficina agente : agentes) {
            agente.start();
        }

        Thread cierre = new Thread(this::esperarClientes, "Cierre-simulacion");

        cierre.setDaemon(true);
        cierre.start();
    }

    private void esperarClientes() {
        try {
            for (AgenteOficina agente : agentes) {
                if (agente instanceof ClienteOficina) {
                    agente.join();
                }
            }

            detener();

            for (AgenteOficina agente : agentes) {
                agente.join();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            detener();

        } finally {
            activa = false;
        }
    }

    public void detener() {
        for (AgenteOficina agente : agentes) {
            agente.interrupt();
        }
    }

    public boolean isActiva() {
        return activa;
    }

    public Parametros getParametros() {
        return parametros;
    }

    public Recepcion getRecepcion() {
        return recepcion;
    }

    public List<AgenteOficina> getAgentes() {
        return Collections.unmodifiableList(agentes);
    }
}