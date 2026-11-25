package office.ui;

import office.agents.OfficeAgent;
import office.model.AgentType;
import office.model.BpmnState;
import office.model.Order;
import office.model.SimulationConfig;
import office.sync.OfficeContext;
import office.sync.SimulationController;
import office.sync.TrackedResource;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Tablero General del Simulador:
 * - Animacion 2D del plano de oficina
 * - Contadores de estados de Threads (Runnable, Timed Waiting, Blocked, Waiting, Terminated)
 * - Contadores totales de agentes por tipo, buferes (m de n) y secciones criticas (esperando y dentro)
 * - Tabla de agentes y sus estados BPMN + Catalogo de estados BPMN
 */
public class DashboardFrame extends JFrame {
    private final SimulationController controller;
    private final OfficeContext context;
    private final OfficeCanvasPanel canvasPanel;

    // Tarjetas de Thread.State
    private JLabel lblRunnable, lblTimedWaiting, lblBlocked, lblWaiting, lblTerminated, lblNew;

    // Modelos de tablas
    private DefaultTableModel threadByTypeModel;
    private DefaultTableModel agentSummaryModel;
    private DefaultTableModel bufferSummaryModel;
    private DefaultTableModel bpmnAgentsModel;
    private DefaultTableModel ordersModel;
    private JTextArea logArea;
    private JLabel lblStatusBadge;

    private final Timer refreshTimer;

    public DashboardFrame(SimulationConfig config) {
        this.controller = new SimulationController(config);
        this.context = controller.getContext();
        this.canvasPanel = new OfficeCanvasPanel(context);

        setTitle("The Office WTC - Tablero General y Simulacion (" + config.getDistributedMode() + ")");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1340, 800);
        setLocationRelativeTo(null);

        initUI();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });

        controller.start();
        refreshTimer = new Timer(80, e -> refreshDashboard());
        refreshTimer.start();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Toolbar superior
        JPanel toolbar = new JPanel(new BorderLayout());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        JButton btnPause = new JButton("Pausar");
        btnPause.addActionListener(e -> {
            boolean next = !context.isPaused();
            context.setPaused(next);
            btnPause.setText(next ? "Reanudar" : "Pausar");
            lblStatusBadge.setText(next ? "PAUSADO" : "EN EJECUCION");
        });

        JButton btnAddClient = new JButton("+ Agregar Cliente");
        btnAddClient.addActionListener(e -> {
            String site = context.getConfig().getDistributedMode() == SimulationConfig.DistributedMode.MATRIZ_SERVIDOR
                    ? "Matriz" : "Sucursal";
            controller.spawnClient(site);
        });

        JButton btnTransfer = new JButton("Trasladar Vendedor (Red)");
        btnTransfer.addActionListener(e -> controller.requestFirstSellerTransfer());

        JButton btnStop = new JButton("Detener");
        btnStop.addActionListener(e -> {
            controller.stop();
            lblStatusBadge.setText("DETENIDO");
        });

        JButton btnReconfig = new JButton("Reconfigurar");
        btnReconfig.addActionListener(e -> {
            SimulationConfig cfg = context.getConfig();
            shutdown();
            dispose();
            new ConfigFrame(cfg).setVisible(true);
        });

        buttons.add(btnPause);
        buttons.add(btnAddClient);
        buttons.add(btnTransfer);
        buttons.add(btnStop);
        buttons.add(btnReconfig);

        lblStatusBadge = new JLabel("EN EJECUCION", SwingConstants.RIGHT);
        lblStatusBadge.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblStatusBadge.setForeground(new Color(22, 128, 57));
        lblStatusBadge.setBorder(new EmptyBorder(0, 0, 0, 12));

        toolbar.add(buttons, BorderLayout.WEST);
        toolbar.add(lblStatusBadge, BorderLayout.EAST);
        root.add(toolbar, BorderLayout.NORTH);

        // Split principal: izquierda plano 2D, derecha pestanas de tableros
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Tablero General (Threads, Agentes y Buferes)", buildGeneralTab());
        tabs.addTab("Tabla BPMN de Agentes", buildBpmnTab());
        tabs.addTab("Pedidos y Bitacora", buildOrdersLogTab());

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvasPanel, tabs);
        split.setResizeWeight(0.54);
        root.add(split, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel buildGeneralTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(6, 6, 6, 6));

        // 1. Estados de los Threads (global)
        JPanel stateCards = new JPanel(new GridLayout(1, 6, 6, 6));
        stateCards.setBorder(BorderFactory.createTitledBorder("Estados de Threads (Global en vivo)"));
        lblRunnable = makeStatCard(stateCards, "RUNNABLE", new Color(220, 252, 231));
        lblTimedWaiting = makeStatCard(stateCards, "TIMED_WAITING", new Color(219, 234, 254));
        lblBlocked = makeStatCard(stateCards, "BLOCKED", new Color(254, 226, 226));
        lblWaiting = makeStatCard(stateCards, "WAITING", new Color(254, 243, 199));
        lblTerminated = makeStatCard(stateCards, "TERMINATED", new Color(226, 232, 240));
        lblNew = makeStatCard(stateCards, "NEW", new Color(243, 232, 255));
        panel.add(stateCards);

        // 2. Desglose de Threads por tipo de agente
        threadByTypeModel = new DefaultTableModel(
                new Object[]{"Tipo de Agente", "RUNNABLE", "TIMED_WAIT", "BLOCKED", "WAITING", "TERMINATED"}, 0);
        JTable tblThreads = new JTable(threadByTypeModel);
        JScrollPane spThreads = new JScrollPane(tblThreads);
        spThreads.setBorder(BorderFactory.createTitledBorder("Threads por Tipo de Agente"));
        panel.add(spThreads);

        // 3. Totales por tipo de agente
        agentSummaryModel = new DefaultTableModel(
                new Object[]{"Resumen de Agentes", "Activos", "Finalizados", "Total"}, 0);
        JTable tblAgents = new JTable(agentSummaryModel);
        JScrollPane spAgents = new JScrollPane(tblAgents);
        spAgents.setBorder(BorderFactory.createTitledBorder("Cantidades Totales de Agentes por Tipo"));
        panel.add(spAgents);

        // 4. Buferes y Secciones Criticas
        bufferSummaryModel = new DefaultTableModel(
                new Object[]{"Bufer / Zona Critica", "Tipo", "Capacidad (m de n)", "Dentro / Uso", "Esperando"}, 0);
        JTable tblBuffers = new JTable(bufferSummaryModel);
        JScrollPane spBuffers = new JScrollPane(tblBuffers);
        spBuffers.setBorder(BorderFactory.createTitledBorder("Buferes y Secciones Criticas (m de n)"));
        panel.add(spBuffers);

        return panel;
    }

    private JPanel buildBpmnTab() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(new EmptyBorder(6, 6, 6, 6));

        bpmnAgentsModel = new DefaultTableModel(
                new Object[]{"ID Agente", "Tipo", "Sede", "Estado BPMN", "Estado Thread", "Pedido", "Detalle"}, 0);
        JTable tblBpmn = new JTable(bpmnAgentsModel);
        JScrollPane spTable = new JScrollPane(tblBpmn);
        spTable.setBorder(BorderFactory.createTitledBorder("Estado BPMN en Vivo por Agente"));

        JTextArea catalog = new JTextArea();
        catalog.setEditable(false);
        catalog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        catalog.setText(
                "ESTADOS BPMN POR TIPO DE AGENTE:\n"
                + "• Cliente (BPMN 01): LLEGANDO -> ESPERANDO_FILA_B3 -> EN_FILA_B3 -> EN_RECEPCION_B1 -> ESPERANDO_RESULTADO -> RECIBIENDO_RESPUESTA -> SALIENDO -> FINALIZADO\n"
                + "• Recepcionista (BPMN 02): ESPERANDO_CLIENTE -> ATENDIENDO_CLIENTE -> VALIDANDO_PEDIDO -> ESPERANDO_B4 -> INSERTANDO_B4 -> [DESCANSO B7]\n"
                + "• Vendedor (BPMN 03): ESPERANDO_PEDIDO_B4 -> COTIZANDO -> ESPERANDO_IMPRESORA_B2 -> IMPRIMIENDO_B2 -> ESPERANDO_B5 -> INSERTANDO_B5 -> [DESCANSO B7 / TRASLADO]\n"
                + "• Supervisor (BPMN 04): ESPERANDO_REVISION_B5 -> REVISANDO_PEDIDO -> ESPERANDO_B6 -> INSERTANDO_B6 -> [DESCANSO B7]\n"
                + "• Administrador (BPMN 05): ESPERANDO_RESULTADO_B6 -> ELABORANDO_COMPROBANTE -> [IMPRESORA B2] -> NOTIFICANDO_CLIENTE -> [DESCANSO B7]\n"
                + "• Sucursal (Distribuido): ESPERANDO_CONEXION -> CONECTADO_SINCRONIZANDO -> DESCONECTADO"
        );
        JScrollPane spCatalog = new JScrollPane(catalog);
        spCatalog.setBorder(BorderFactory.createTitledBorder("Catalogo de Estados BPMN"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spTable, spCatalog);
        split.setResizeWeight(0.72);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildOrdersLogTab() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(new EmptyBorder(6, 6, 6, 6));

        ordersModel = new DefaultTableModel(
                new Object[]{"Pedido #", "Cliente", "Perfil", "Unidades", "Importe", "Estado", "Motivo"}, 0);
        JTable tblOrders = new JTable(ordersModel);
        JScrollPane spOrders = new JScrollPane(tblOrders);
        spOrders.setBorder(BorderFactory.createTitledBorder("Pedidos Procesados"));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane spLogs = new JScrollPane(logArea);
        spLogs.setBorder(BorderFactory.createTitledBorder("Bitacora de Eventos"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spOrders, spLogs);
        split.setResizeWeight(0.5);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JLabel makeStatCard(JPanel parent, String title, Color bg) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 205)),
                new EmptyBorder(4, 4, 4, 4)
        ));
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 10));
        JLabel lblValue = new JLabel("0", SwingConstants.CENTER);
        lblValue.setFont(new Font("SansSerif", Font.BOLD, 18));
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        parent.add(card);
        return lblValue;
    }

    private void refreshDashboard() {
        canvasPanel.repaint();

        List<OfficeAgent> agents = context.getAgents();
        Map<Thread.State, Integer> globalStates = new EnumMap<>(Thread.State.class);
        for (Thread.State s : Thread.State.values()) {
            globalStates.put(s, 0);
        }

        Map<AgentType, Map<Thread.State, Integer>> byTypeStates = new EnumMap<>(AgentType.class);
        Map<AgentType, Integer> activeByType = new EnumMap<>(AgentType.class);
        Map<AgentType, Integer> totalByType = new EnumMap<>(AgentType.class);

        for (AgentType t : AgentType.values()) {
            Map<Thread.State, Integer> map = new EnumMap<>(Thread.State.class);
            for (Thread.State s : Thread.State.values()) map.put(s, 0);
            byTypeStates.put(t, map);
            activeByType.put(t, 0);
            totalByType.put(t, 0);
        }

        for (OfficeAgent a : agents) {
            Thread.State st = a.getState();
            globalStates.put(st, globalStates.get(st) + 1);

            AgentType type = a.getAgentType();
            Map<Thread.State, Integer> tmap = byTypeStates.get(type);
            tmap.put(st, tmap.get(st) + 1);
            totalByType.put(type, totalByType.get(type) + 1);
            if (st != Thread.State.TERMINATED) {
                activeByType.put(type, activeByType.get(type) + 1);
            }
        }

        lblRunnable.setText(String.valueOf(globalStates.get(Thread.State.RUNNABLE)));
        lblTimedWaiting.setText(String.valueOf(globalStates.get(Thread.State.TIMED_WAITING)));
        lblBlocked.setText(String.valueOf(globalStates.get(Thread.State.BLOCKED)));
        lblWaiting.setText(String.valueOf(globalStates.get(Thread.State.WAITING)));
        lblTerminated.setText(String.valueOf(globalStates.get(Thread.State.TERMINATED)));
        lblNew.setText(String.valueOf(globalStates.get(Thread.State.NEW)));

        // Tabla threads por tipo
        threadByTypeModel.setRowCount(0);
        for (AgentType t : AgentType.values()) {
            Map<Thread.State, Integer> m = byTypeStates.get(t);
            threadByTypeModel.addRow(new Object[]{
                    t.getDisplayName(),
                    m.get(Thread.State.RUNNABLE),
                    m.get(Thread.State.TIMED_WAITING),
                    m.get(Thread.State.BLOCKED),
                    m.get(Thread.State.WAITING),
                    m.get(Thread.State.TERMINATED)
            });
        }

        // Tabla resumen agentes por tipo
        agentSummaryModel.setRowCount(0);
        for (AgentType t : AgentType.values()) {
            int total = totalByType.get(t);
            int active = activeByType.get(t);
            String label = "Agente " + t.getDisplayName() + ": hay " + total + " agentes";
            if (t == AgentType.SUCURSAL) {
                label += " (Conectadas: " + context.getConnectedBranches() + ")";
            }
            agentSummaryModel.addRow(new Object[]{label, active, total - active, total});
        }

        // Tabla buferes y zonas criticas
        bufferSummaryModel.setRowCount(0);
        for (TrackedResource res : context.getAllResources()) {
            var cfg = res.getConfig();
            bufferSummaryModel.addRow(new Object[]{
                    cfg.getCode() + ". " + cfg.getName(),
                    cfg.getKind(),
                    cfg.getM() + " de " + cfg.getN(),
                    "Hay " + res.getCurrentCount() + " en " + cfg.getCode(),
                    "Hay " + res.getWaitingCount() + " esperando"
            });
        }

        // Tabla BPMN de agentes
        bpmnAgentsModel.setRowCount(0);
        for (OfficeAgent a : agents) {
            bpmnAgentsModel.addRow(new Object[]{
                    a.getAgentId(),
                    a.getAgentType().getDisplayName(),
                    a.getSiteName(),
                    a.getBpmnState().getLabel(),
                    a.getState().name(),
                    a.getCurrentOrderId(),
                    a.getDetail()
            });
        }

        // Tabla pedidos
        ordersModel.setRowCount(0);
        for (Order o : context.getCompletedOrders()) {
            ordersModel.addRow(new Object[]{
                    "#" + o.getId(),
                    o.getClientId(),
                    o.getProfile().getLabel(),
                    o.getTotalUnits(),
                    String.format("$%.2f", o.getTotalAmount()),
                    o.isApproved() ? "Aprobado" : "Rechazado",
                    o.getReason()
            });
        }

        // Bitacora
        List<String> logs = context.getRecentLogs();
        String joined = String.join("\n", logs);
        if (!joined.equals(logArea.getText())) {
            logArea.setText(joined);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }

    private void shutdown() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        controller.stop();
    }
}
