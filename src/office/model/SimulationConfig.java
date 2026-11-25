package office.model;

/**
 * Configuracion general del simulador "The Office WTC".
 * Incluye cantidades de agentes, timers, capacidades (m de n) de secciones criticas y modo distribuido.
 */
public class SimulationConfig {
    public enum DistributedMode {
        MATRIZ_SERVIDOR("Matriz (Servidor Socket)"),
        SUCURSAL_CLIENTE("Sucursal (Agente Cliente Socket)");

        private final String label;

        DistributedMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    // Cantidades de agentes
    private int clientCount = 6;
    private int receptionistCount = 1;
    private int sellerCount = 1;
    private int supervisorCount = 1;
    private int adminCount = 1;

    // Regla del supervisor
    private int supervisorUnitLimit = 50;
    private int ordersBeforeRest = 3;

    // Timers (ms)
    private int clientArrivalIntervalMs = 1300;
    private int receptionTimeMs = 1100;
    private int sellerQuoteTimeMs = 1300;
    private int printerTimeMs = 800;
    private int supervisorReviewTimeMs = 1000;
    private int adminProcessTimeMs = 900;
    private int restDurationMs = 1600;
    private int animationStepMs = 30;

    // Búferes y Zonas críticas (m de n, donde m <= n)
    private BufferConfig b1ReceptionDesk = new BufferConfig("B1", "Puesto de recepcion", "Bufer 1", 1, 1);
    private BufferConfig b2Printer = new BufferConfig("B2", "Impresora de sede", "Bufer 1", 1, 1);
    private BufferConfig b3ReceptionQueue = new BufferConfig("B3", "Fila de recepcion", "Zona critica", 3, 10);
    private BufferConfig b4RequestTray = new BufferConfig("B4", "Bandeja de solicitudes", "Bufer m (Monitor)", 5, 15);
    private BufferConfig b5ReviewTray = new BufferConfig("B5", "Bandeja de revision", "Bufer m (Lock)", 3, 15);
    private BufferConfig b6ResultTray = new BufferConfig("B6", "Bandeja de resultados", "Bufer m (Lock)", 5, 15);
    private BufferConfig b7RestZone = new BufferConfig("B7", "Zona de descanso", "Zona critica", 3, 10);

    // Contexto distribuido
    private DistributedMode distributedMode = DistributedMode.MATRIZ_SERVIDOR;
    private String host = "localhost";
    private int port = 5000;

    public int getClientCount() { return clientCount; }
    public void setClientCount(int clientCount) { this.clientCount = Math.max(1, clientCount); }

    public int getReceptionistCount() { return receptionistCount; }
    public void setReceptionistCount(int receptionistCount) { this.receptionistCount = Math.max(1, receptionistCount); }

    public int getSellerCount() { return sellerCount; }
    public void setSellerCount(int sellerCount) { this.sellerCount = Math.max(0, sellerCount); }

    public int getSupervisorCount() { return supervisorCount; }
    public void setSupervisorCount(int supervisorCount) { this.supervisorCount = Math.max(0, supervisorCount); }

    public int getAdminCount() { return adminCount; }
    public void setAdminCount(int adminCount) { this.adminCount = Math.max(0, adminCount); }

    public int getSupervisorUnitLimit() { return supervisorUnitLimit; }
    public void setSupervisorUnitLimit(int supervisorUnitLimit) { this.supervisorUnitLimit = Math.max(1, supervisorUnitLimit); }

    public int getOrdersBeforeRest() { return ordersBeforeRest; }
    public void setOrdersBeforeRest(int ordersBeforeRest) { this.ordersBeforeRest = Math.max(1, ordersBeforeRest); }

    public int getClientArrivalIntervalMs() { return clientArrivalIntervalMs; }
    public void setClientArrivalIntervalMs(int clientArrivalIntervalMs) { this.clientArrivalIntervalMs = Math.max(100, clientArrivalIntervalMs); }

    public int getReceptionTimeMs() { return receptionTimeMs; }
    public void setReceptionTimeMs(int receptionTimeMs) { this.receptionTimeMs = Math.max(100, receptionTimeMs); }

    public int getSellerQuoteTimeMs() { return sellerQuoteTimeMs; }
    public void setSellerQuoteTimeMs(int sellerQuoteTimeMs) { this.sellerQuoteTimeMs = Math.max(100, sellerQuoteTimeMs); }

    public int getPrinterTimeMs() { return printerTimeMs; }
    public void setPrinterTimeMs(int printerTimeMs) { this.printerTimeMs = Math.max(100, printerTimeMs); }

    public int getSupervisorReviewTimeMs() { return supervisorReviewTimeMs; }
    public void setSupervisorReviewTimeMs(int supervisorReviewTimeMs) { this.supervisorReviewTimeMs = Math.max(100, supervisorReviewTimeMs); }

    public int getAdminProcessTimeMs() { return adminProcessTimeMs; }
    public void setAdminProcessTimeMs(int adminProcessTimeMs) { this.adminProcessTimeMs = Math.max(100, adminProcessTimeMs); }

    public int getRestDurationMs() { return restDurationMs; }
    public void setRestDurationMs(int restDurationMs) { this.restDurationMs = Math.max(100, restDurationMs); }

    public int getAnimationStepMs() { return animationStepMs; }
    public void setAnimationStepMs(int animationStepMs) { this.animationStepMs = Math.max(10, animationStepMs); }

    public BufferConfig getB1ReceptionDesk() { return b1ReceptionDesk; }
    public BufferConfig getB2Printer() { return b2Printer; }
    public BufferConfig getB3ReceptionQueue() { return b3ReceptionQueue; }
    public BufferConfig getB4RequestTray() { return b4RequestTray; }
    public BufferConfig getB5ReviewTray() { return b5ReviewTray; }
    public BufferConfig getB6ResultTray() { return b6ResultTray; }
    public BufferConfig getB7RestZone() { return b7RestZone; }

    public DistributedMode getDistributedMode() { return distributedMode; }
    public void setDistributedMode(DistributedMode distributedMode) { this.distributedMode = distributedMode; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = (host == null || host.isBlank()) ? "localhost" : host.trim(); }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = Math.max(1024, Math.min(65535, port)); }
}
