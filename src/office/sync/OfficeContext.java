package office.sync;

import office.agents.OfficeAgent;
import office.model.Order;
import office.model.SimulationConfig;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contexto compartido de la simulacion: contiene los recursos B1..B7, lista de agentes y control de ciclo.
 */
public class OfficeContext {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final SimulationConfig config;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final AtomicInteger orderSeq = new AtomicInteger(1);
    private final AtomicInteger clientSeq = new AtomicInteger(1);

    // Secciones criticas y buferes
    private final SemaphoreZone b1ReceptionDesk;
    private final SemaphoreZone b2Printer;
    private final SemaphoreZone b3ReceptionQueue;
    private final MonitorBuffer<Order> b4RequestTray;
    private final LockBuffer<Order> b5ReviewTray;
    private final LockBuffer<Order> b6ResultTray;
    private final SemaphoreZone b7RestZone;

    private final ReceptionDeskHandoff deskHandoff;

    private final List<TrackedResource> allResources;
    private final List<OfficeAgent> agents = new CopyOnWriteArrayList<>();
    private final List<Order> completedOrders = new CopyOnWriteArrayList<>();
    private final List<String> eventLogs = Collections.synchronizedList(new ArrayList<>());

    // Conteo para modo distribuido
    private final AtomicInteger connectedBranches = new AtomicInteger(0);

    public OfficeContext(SimulationConfig config) {
        this.config = config;
        this.b1ReceptionDesk = new SemaphoreZone(config.getB1ReceptionDesk());
        this.b2Printer = new SemaphoreZone(config.getB2Printer());
        this.b3ReceptionQueue = new SemaphoreZone(config.getB3ReceptionQueue());
        this.b4RequestTray = new MonitorBuffer<>(config.getB4RequestTray(), running);
        this.b5ReviewTray = new LockBuffer<>(config.getB5ReviewTray(), running);
        this.b6ResultTray = new LockBuffer<>(config.getB6ResultTray(), running);
        this.b7RestZone = new SemaphoreZone(config.getB7RestZone());
        this.deskHandoff = new ReceptionDeskHandoff(running);

        this.allResources = List.of(
                b1ReceptionDesk, b2Printer, b3ReceptionQueue,
                b4RequestTray, b5ReviewTray, b6ResultTray, b7RestZone
        );
    }

    public SimulationConfig getConfig() { return config; }
    public AtomicBoolean getRunningFlag() { return running; }
    public boolean isRunning() { return running.get(); }
    public boolean isPaused() { return paused.get(); }

    public void setPaused(boolean value) {
        paused.set(value);
        if (!value) {
            synchronized (paused) {
                paused.notifyAll();
            }
        }
    }

    public void checkPause() throws InterruptedException {
        synchronized (paused) {
            while (paused.get() && running.get()) {
                paused.wait();
            }
        }
    }

    public void stopAll() {
        running.set(false);
        setPaused(false);
        deskHandoff.wakeAll();
        for (TrackedResource resource : allResources) {
            resource.wakeAll();
        }
        for (Order order : completedOrders) {
            order.wakeUp();
        }
        for (OfficeAgent agent : agents) {
            agent.requestStop();
            agent.interrupt();
        }
    }

    public int nextOrderId() {
        return orderSeq.getAndIncrement();
    }

    public int nextClientNumber() {
        return clientSeq.getAndIncrement();
    }

    public void registerAgent(OfficeAgent agent) {
        agents.add(agent);
    }

    public List<OfficeAgent> getAgents() {
        return agents;
    }

    public List<TrackedResource> getAllResources() {
        return allResources;
    }

    public SemaphoreZone getB1ReceptionDesk() { return b1ReceptionDesk; }
    public SemaphoreZone getB2Printer() { return b2Printer; }
    public SemaphoreZone getB3ReceptionQueue() { return b3ReceptionQueue; }
    public MonitorBuffer<Order> getB4RequestTray() { return b4RequestTray; }
    public LockBuffer<Order> getB5ReviewTray() { return b5ReviewTray; }
    public LockBuffer<Order> getB6ResultTray() { return b6ResultTray; }
    public SemaphoreZone getB7RestZone() { return b7RestZone; }
    public ReceptionDeskHandoff getDeskHandoff() { return deskHandoff; }

    public void recordCompletedOrder(Order order) {
        completedOrders.add(order);
    }

    public List<Order> getCompletedOrders() {
        return completedOrders;
    }

    public int getConnectedBranches() {
        return connectedBranches.get();
    }

    public void setConnectedBranches(int count) {
        connectedBranches.set(Math.max(0, count));
    }

    public void incrementConnectedBranches() {
        connectedBranches.incrementAndGet();
    }

    public void decrementConnectedBranches() {
        connectedBranches.updateAndGet(v -> Math.max(0, v - 1));
    }

    public void log(String message) {
        String entry = "[" + LocalTime.now().format(TIME_FMT) + "] " + message;
        synchronized (eventLogs) {
            eventLogs.add(entry);
            if (eventLogs.size() > 150) {
                eventLogs.remove(0);
            }
        }
    }

    public List<String> getRecentLogs() {
        synchronized (eventLogs) {
            return new ArrayList<>(eventLogs);
        }
    }
}
