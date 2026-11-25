package office.model;

/**
 * Tipos de agentes en el simulador "The Office WTC".
 */
public enum AgentType {
    CLIENTE("Cliente"),
    RECEPCIONISTA("Recepcionista"),
    VENDEDOR("Vendedor"),
    SUPERVISOR("Supervisor"),
    ADMINISTRADOR("Administrador"),
    SUCURSAL("Sucursal (Distribuido)");

    private final String displayName;

    AgentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
