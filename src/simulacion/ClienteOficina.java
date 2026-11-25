package simulacion;

import java.awt.Point;

public class ClienteOficina extends AgenteOficina {
    private final Parametros parametros;
    private final Recepcion recepcion;
    private final Solicitud solicitud;

    public ClienteOficina(int numero, Parametros parametros, Recepcion recepcion) {
        super("Cliente", numero, numero);
        this.parametros = parametros;
        this.recepcion = recepcion;
        String[] tipos = {"Escuela", "Papeleria", "Particular"};
        solicitud = new Solicitud(numero, tipos[(numero - 1) % tipos.length]);
        colocar(40 + ((numero - 1) % 10) * 30, 80 + ((numero - 1) / 10) * 38);
    }

    public Solicitud getSolicitud() {
        return solicitud;
    }

    @Override
    protected void ejecutar() throws InterruptedException {
        try {
            pausar("Llegar: " + solicitud.getTipoCliente(), (solicitud.getNumero() - 1) * parametros.getTiempo(0));
            mostrar(true);

            int lugar = recepcion.entrar(solicitud, this);

            caminar(450, 200 + lugar * 50, "Caminar hacia B3");
            recepcion.llegarFila(solicitud);

            if (!solicitud.esperarLlamado(this)) {
                cambiarEstado("Atencion cancelada");
                return;
            }

            Point puesto = solicitud.getDestinoB1();

            caminar(puesto.x, puesto.y, "Caminar hacia B1");
            solicitud.llegarPuesto();

            if (!solicitud.esperarRespuesta(this)) {
                cambiarEstado("Atencion cancelada");
                return;
            }

            caminar(690, puesto.y, "Salir de B1");
            solicitud.salirPuesto();

            caminar(690, 470, "Caminar hacia la salida");
            caminar(950, 470, "Caminar hacia la salida");

            cambiarEstado("Finalizado: pedido #" + solicitud.getNumero());

        } finally {
            recepcion.retirar(solicitud);
            mostrar(false);
        }
    }
}