package office.agents;

import office.model.AgentType;
import office.model.BpmnState;
import office.model.OfficeLayout;
import office.model.SimulationConfig;
import office.sync.OfficeContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Agente Distribuido (computadora aparte: Matriz <-> Sucursal por Sockets TCP).
 * En modo Matriz actua como agente coordinador de red (ServerSocket).
 * En modo Sucursal actua como agente cliente de red (Socket) distinto del agente Cliente de negocio.
 */
public class DistributedBranchAgent extends OfficeAgent {
    private final SimulationConfig.DistributedMode mode;
    private final String host;
    private final int port;

    private ServerSocket serverSocket;
    private final List<PrintWriter> clientWriters = new CopyOnWriteArrayList<>();
    private PrintWriter remoteOut;

    public DistributedBranchAgent(OfficeContext context, String agentId, SimulationConfig.DistributedMode mode, String host, int port) {
        super(context, agentId, AgentType.SUCURSAL,
                mode == SimulationConfig.DistributedMode.MATRIZ_SERVIDOR ? "Matriz" : "Sucursal",
                OfficeLayout.BRANCH_GATE);
        this.mode = mode;
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        if (mode == SimulationConfig.DistributedMode.MATRIZ_SERVIDOR) {
            runServerCoordinator();
        } else {
            runBranchClient();
        }
    }

    private void runServerCoordinator() {
        setBpmnState(BpmnState.ESPERANDO_CONEXION);
        setDetail("Escuchando sucursales en puerto " + port);
        try {
            serverSocket = new ServerSocket(port);
            context.log(agentId + " (Matriz) escuchando sockets en puerto " + port + ".");
            while (shouldKeepRunning() && !serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                context.incrementConnectedBranches();
                setBpmnState(BpmnState.CONECTADO_SINCRONIZANDO);
                setDetail("Sucursales conectadas: " + context.getConnectedBranches());
                Thread handler = new Thread(() -> handleIncomingBranch(socket), "BranchHandler-" + socket.getPort());
                handler.setDaemon(true);
                handler.start();
            }
        } catch (IOException e) {
            if (shouldKeepRunning()) {
                setBpmnState(BpmnState.DESCONECTADO);
                setDetail("Puerto " + port + " ocupado o cerrado");
                context.log(agentId + " aviso de socket: " + e.getMessage());
            }
        } finally {
            closeServer();
            setBpmnState(BpmnState.FINALIZADO);
        }
    }

    private void handleIncomingBranch(Socket socket) {
        PrintWriter out = null;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out = new PrintWriter(socket.getOutputStream(), true);
            clientWriters.add(out);
            String line;
            while (shouldKeepRunning() && (line = in.readLine()) != null) {
                if (line.startsWith("SYNC//")) {
                    context.log("[Red] Recibido de sucursal: " + line.substring(6));
                } else if (line.startsWith("HELLO//")) {
                    context.log("[Red] Sucursal conectada: " + line.substring(7));
                    out.println("ACK//Matriz conectada");
                }
            }
        } catch (IOException ignored) {
        } finally {
            if (out != null) clientWriters.remove(out);
            context.decrementConnectedBranches();
            if (context.getConnectedBranches() == 0 && shouldKeepRunning()) {
                setBpmnState(BpmnState.ESPERANDO_CONEXION);
                setDetail("Escuchando sucursales en puerto " + port);
            } else {
                setDetail("Sucursales conectadas: " + context.getConnectedBranches());
            }
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private void runBranchClient() {
        while (shouldKeepRunning()) {
            setBpmnState(BpmnState.ESPERANDO_CONEXION);
            setDetail("Conectando a Matriz " + host + ":" + port);
            try (Socket socket = new Socket(host, port);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                this.remoteOut = out;
                context.setConnectedBranches(1);
                setBpmnState(BpmnState.CONECTADO_SINCRONIZANDO);
                setDetail("Conectado a Matriz (" + host + ":" + port + ")");
                out.println("HELLO//" + agentId);
                context.log(agentId + " conectado a Matriz " + host + ":" + port + ".");

                Thread readerThread = new Thread(() -> {
                    try {
                        String msg;
                        while (shouldKeepRunning() && (msg = in.readLine()) != null) {
                            handleRemoteCommand(msg);
                        }
                    } catch (IOException ignored) {}
                }, "BranchSocketReader");
                readerThread.setDaemon(true);
                readerThread.start();

                while (shouldKeepRunning() && !socket.isClosed()) {
                    out.println("SYNC//Agentes=" + context.getAgents().size()
                            + ",Completados=" + context.getCompletedOrders().size());
                    workSleep(2000);
                }
            } catch (IOException e) {
                context.setConnectedBranches(0);
                this.remoteOut = null;
                setBpmnState(BpmnState.DESCONECTADO);
                setDetail("Reintentando conexion a " + host + ":" + port);
                try {
                    workSleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        setBpmnState(BpmnState.FINALIZADO);
    }

    private void handleRemoteCommand(String msg) {
        if (msg.startsWith("TRANSFER_SELLER//")) {
            String sellerId = msg.substring("TRANSFER_SELLER//".length()).trim();
            context.log("[Red] Recibido traslado de " + sellerId + " desde Matriz.");
            SellerAgent incoming = new SellerAgent(context, sellerId + "-Suc", "Sucursal");
            context.registerAgent(incoming);
            incoming.start();
        } else if (msg.startsWith("ACK//")) {
            context.log("[Red] " + msg.substring(5));
        }
    }

    public void sendSellerTransfer(String sellerId) {
        if (mode == SimulationConfig.DistributedMode.MATRIZ_SERVIDOR) {
            for (PrintWriter writer : clientWriters) {
                writer.println("TRANSFER_SELLER//" + sellerId);
            }
        } else if (remoteOut != null) {
            remoteOut.println("SYNC//Solicitud de traslado " + sellerId);
        }
    }

    @Override
    public void requestStop() {
        super.requestStop();
        closeServer();
    }

    private void closeServer() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {}
        }
    }
}
