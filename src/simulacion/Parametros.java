package simulacion;

public class Parametros {
    public static final String[] TIEMPOS = {
            "Llegada de clientes", "Atencion en B1", "Registro de solicitud",
            "Cotizacion (pendiente)", "Revision (pendiente)",
            "Impresion (pendiente)", "Descanso (pendiente)"
    };

    public static final String[] RECURSOS = {
            "B1 Atencion", "B2 Impresora", "B3 Fila",
            "B4 Solicitudes", "B5 Revision", "B6 Resultados", "B7 Descanso"
    };

    public static final int[] MAXIMOS = {1, 1, 3, 5, 3, 5, 3};

    private final int clientes, recepcionistas;
    private final int[] tiempos, capacidades;

    public Parametros(int clientes, int recepcionistas, int[] tiempos, int[] capacidades) {

        if (clientes < 1 || clientes > 100 || recepcionistas < 1 || recepcionistas > 10) {
            throw new IllegalArgumentException("Cantidades de agentes no validas");
        }

        if (tiempos.length != TIEMPOS.length || capacidades.length != MAXIMOS.length) {
            throw new IllegalArgumentException("Faltan parametros");
        }

        for (int tiempo : tiempos) {
            if (tiempo < 0 || tiempo > 30000) {
                throw new IllegalArgumentException("Tiempo fuera del rango 0 a 30000 ms");
            }
        }

        for (int i = 0; i < capacidades.length; i++) {
            if (capacidades[i] < 1 || capacidades[i] > MAXIMOS[i]) {
                throw new IllegalArgumentException("Capacidad no valida: " + RECURSOS[i]);
            }
        }

        this.clientes = clientes;
        this.recepcionistas = recepcionistas;
        this.tiempos = tiempos.clone();
        this.capacidades = capacidades.clone();
    }

    public int getClientes() {
        return clientes;
    }

    public int getRecepcionistas() {
        return recepcionistas;
    }

    public int getTiempo(int posicion) {
        return tiempos[posicion];
    }

    public int getCapacidad(int posicion) {
        return capacidades[posicion];
    }
}