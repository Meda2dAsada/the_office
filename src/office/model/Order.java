package office.model;

/**
 * Representa un pedido compartido entre las etapas de la oficina.
 * No es un agente; conserva su identificador y el del cliente que lo solicito.
 */
public class Order {
    public enum ClientProfile {
        ESCUELA("Escuela"),
        PAPELERIA("Papeleria"),
        PARTICULAR("Particular");

        private final String label;

        ClientProfile(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private int id;
    private final String clientId;
    private final ClientProfile profile;
    private final int hojas;
    private final int cuadernos;
    private final int carpetas;

    private double totalAmount;
    private boolean approved;
    private String reason = "";
    private boolean completed;

    public Order(String clientId, ClientProfile profile, int hojas, int cuadernos, int carpetas) {
        this.clientId = clientId;
        this.profile = profile;
        this.hojas = hojas;
        this.cuadernos = cuadernos;
        this.carpetas = carpetas;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getClientId() { return clientId; }
    public ClientProfile getProfile() { return profile; }

    public int getHojas() { return hojas; }
    public int getCuadernos() { return cuadernos; }
    public int getCarpetas() { return carpetas; }

    public int getTotalUnits() {
        return hojas + cuadernos + carpetas;
    }

    public boolean hasValidItems() {
        return hojas > 0 && cuadernos >= 0 && carpetas >= 0 && getTotalUnits() > 0;
    }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public synchronized boolean isCompleted() {
        return completed;
    }

    /**
     * Marca la atencion como terminada y notifica al agente Cliente que espera su respuesta.
     */
    public synchronized void markCompleted() {
        this.completed = true;
        notifyAll();
    }

    /**
     * El agente Cliente espera aqui hasta que el Administrador (o cierre) notifique el resultado.
     */
    public synchronized void awaitCompletion() throws InterruptedException {
        while (!completed && !Thread.currentThread().isInterrupted()) {
            wait();
        }
    }

    public synchronized void wakeUp() {
        notifyAll();
    }
}
