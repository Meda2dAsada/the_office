package simulacion;

public class Recepcionista extends AgenteOficina {
    private final Parametros parametros;
    private final Recepcion recepcion;

    public Recepcionista(int numero, Parametros parametros, Recepcion recepcion) {
        super("Recepcionista", numero, parametros.getClientes() + numero);
        this.parametros = parametros;
        this.recepcion = recepcion;
        int y = 250 + (2 * numero - parametros.getRecepcionistas() - 1) * 20;
        colocar(780, y);
        mostrar(true);
    }

    @Override
    protected void ejecutar() throws InterruptedException {
        while (!isInterrupted()) {
            Solicitud solicitud = recepcion.siguiente(this);

            try {
                if (solicitud.esperarLlegada(this)) {
                    pausar("Revisar solicitud #" + solicitud.getNumero(), parametros.getTiempo(1));
                    pausar("Registrar pedido #" + solicitud.getNumero(), parametros.getTiempo(2));
                    recepcion.registrar(solicitud);
                    solicitud.esperarSalida(this);
                }

            } finally {
                solicitud.finalizar(false);
                recepcion.liberarPuesto(solicitud);
            }
        }
    }
}