package office.model;

/**
 * Estados BPMN de los agentes de "The Office WTC".
 */
public enum BpmnState {
    // Ciclo general
    INICIO("Inicio"),
    FINALIZADO("Finalizado"),

    // Cliente (BPMN 01)
    LLEGANDO("Caminando a entrada"),
    ESPERANDO_FILA_B3("Esperando cupo en B3"),
    EN_FILA_B3("Esperando turno en fila B3"),
    CAMINANDO_MOSTRADOR("Caminando al mostrador B1"),
    EN_RECEPCION_B1("Presentando solicitud en B1"),
    ESPERANDO_RESULTADO("Esperando resultado del pedido"),
    RECIBIENDO_RESPUESTA("Recibiendo comprobante/motivo"),
    SALIENDO("Caminando hacia la salida"),

    // Recepcionista (BPMN 02)
    ESPERANDO_CLIENTE("Esperando cliente en B1"),
    ATENDIENDO_CLIENTE("Atendiendo cliente en B1"),
    VALIDANDO_PEDIDO("Validando productos y cantidades"),
    ESPERANDO_B4("Esperando cupo en B4"),
    INSERTANDO_B4("Depositando pedido en B4"),

    // Vendedor (BPMN 03)
    ESPERANDO_PEDIDO_B4("Esperando pedido en B4"),
    COTIZANDO("Calculando importe en escritorio"),
    ESPERANDO_IMPRESORA_B2("Esperando impresora B2"),
    IMPRIMIENDO_B2("Imprimiendo en B2"),
    ESPERANDO_B5("Esperando cupo en B5"),
    INSERTANDO_B5("Colocando cotizacion en B5"),
    TRASLADANDO_SEDE("En traslado entre sedes"),

    // Supervisor (BPMN 04)
    ESPERANDO_REVISION_B5("Esperando cotizacion en B5"),
    REVISANDO_PEDIDO("Sumando unidades (limite)"),
    ESPERANDO_B6("Esperando cupo en B6"),
    INSERTANDO_B6("Colocando resultado en B6"),

    // Administrador (BPMN 05)
    ESPERANDO_RESULTADO_B6("Esperando resultado en B6"),
    ELABORANDO_COMPROBANTE("Elaborando comprobante / motivo"),
    NOTIFICANDO_CLIENTE("Notificando resultado al cliente"),

    // Descanso compartido (B7)
    ESPERANDO_DESCANSO_B7("Esperando cupo en descanso B7"),
    EN_DESCANSO_B7("En zona de descanso B7"),

    // Agente Distribuido (Sucursal / Matriz)
    ESPERANDO_CONEXION("Esperando conexion socket"),
    CONECTADO_SINCRONIZANDO("Sincronizando sedes por socket"),
    DESCONECTADO("Desconectado");

    private final String label;

    BpmnState(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
