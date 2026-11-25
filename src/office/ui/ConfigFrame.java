package office.ui;

import office.model.BufferConfig;
import office.model.SimulationConfig;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana de configuracion inicial de entradas de datos del simulador:
 * cantidades de agentes, todos los timers, capacidades (m de n) de buferes/secciones criticas y modo distribuido.
 */
public class ConfigFrame extends JFrame {
    private final SimulationConfig config;

    // Agentes
    private JSpinner spClients, spReceptionists, spSellers, spSupervisors, spAdmins;
    private JSpinner spUnitLimit, spOrdersRest;

    // Timers
    private JSpinner spArrivalMs, spReceptionMs, spQuoteMs, spPrinterMs;
    private JSpinner spSupervisorMs, spAdminMs, spRestMs, spAnimMs;

    // Distribuido
    private JComboBox<SimulationConfig.DistributedMode> cbMode;
    private JTextField tfHost;
    private JSpinner spPort;

    // Filas m de n
    private final List<BufferRow> bufferRows = new ArrayList<>();

    private static class BufferRow {
        final BufferConfig cfg;
        final JSpinner spM;
        final JSpinner spN;
        final JLabel lblStatus;

        BufferRow(BufferConfig cfg, JSpinner spM, JSpinner spN, JLabel lblStatus) {
            this.cfg = cfg;
            this.spM = spM;
            this.spN = spN;
            this.lblStatus = lblStatus;
        }
    }

    public ConfigFrame() {
        this(new SimulationConfig());
    }

    public ConfigFrame(SimulationConfig initialConfig) {
        this.config = initialConfig;
        setTitle("The Office WTC - Configuracion del Simulador (2o Avance)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 740);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Configuracion de Entradas - The Office WTC");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        JLabel subtitle = new JLabel("Ajusta agentes, timers (ms), secciones criticas (m de n, donde m <= n) y red distribuida.");
        subtitle.setForeground(new Color(80, 80, 90));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        // Center content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(buildAgentsPanel());
        content.add(Box.createVerticalStrut(8));
        content.add(buildTimersPanel());
        content.add(Box.createVerticalStrut(8));
        content.add(buildBuffersPanel());
        content.add(Box.createVerticalStrut(8));
        content.add(buildDistributedPanel());

        JScrollPane scroll = new JScrollPane(content);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scroll, BorderLayout.CENTER);

        // Footer buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        JButton btnBasic = new JButton("Preset Basico (2 Agentes)");
        btnBasic.addActionListener(e -> applyBasicPreset());

        JButton btnFull = new JButton("Preset Oficina Completa");
        btnFull.addActionListener(e -> applyFullPreset());

        JButton btnStart = new JButton("Iniciar Simulador");
        btnStart.setBackground(new Color(33, 118, 210));
        btnStart.setForeground(Color.WHITE);
        btnStart.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnStart.addActionListener(e -> onStart());

        footer.add(btnBasic);
        footer.add(btnFull);
        footer.add(btnStart);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildAgentsPanel() {
        JPanel p = new JPanel(new GridLayout(0, 4, 10, 6));
        p.setBorder(BorderFactory.createTitledBorder("1. Agentes y Reglas de Oficina"));

        spClients = makeSpinner(config.getClientCount(), 1, 100, 1);
        spReceptionists = makeSpinner(config.getReceptionistCount(), 1, 10, 1);
        spSellers = makeSpinner(config.getSellerCount(), 0, 10, 1);
        spSupervisors = makeSpinner(config.getSupervisorCount(), 0, 10, 1);
        spAdmins = makeSpinner(config.getAdminCount(), 0, 10, 1);
        spUnitLimit = makeSpinner(config.getSupervisorUnitLimit(), 1, 500, 5);
        spOrdersRest = makeSpinner(config.getOrdersBeforeRest(), 1, 20, 1);

        p.add(new JLabel("Clientes (total):")); p.add(spClients);
        p.add(new JLabel("Recepcionistas:")); p.add(spReceptionists);
        p.add(new JLabel("Vendedores:")); p.add(spSellers);
        p.add(new JLabel("Supervisores:")); p.add(spSupervisors);
        p.add(new JLabel("Administradores:")); p.add(spAdmins);
        p.add(new JLabel("Limite unidades (Supervisor):")); p.add(spUnitLimit);
        p.add(new JLabel("Pedidos antes de descanso:")); p.add(spOrdersRest);
        p.add(new JLabel("")); p.add(new JLabel(""));

        return p;
    }

    private JPanel buildTimersPanel() {
        JPanel p = new JPanel(new GridLayout(0, 4, 10, 6));
        p.setBorder(BorderFactory.createTitledBorder("2. Variables de Timers (milisegundos)"));

        spArrivalMs = makeSpinner(config.getClientArrivalIntervalMs(), 100, 15000, 100);
        spReceptionMs = makeSpinner(config.getReceptionTimeMs(), 100, 15000, 100);
        spQuoteMs = makeSpinner(config.getSellerQuoteTimeMs(), 100, 15000, 100);
        spPrinterMs = makeSpinner(config.getPrinterTimeMs(), 100, 15000, 100);
        spSupervisorMs = makeSpinner(config.getSupervisorReviewTimeMs(), 100, 15000, 100);
        spAdminMs = makeSpinner(config.getAdminProcessTimeMs(), 100, 15000, 100);
        spRestMs = makeSpinner(config.getRestDurationMs(), 100, 15000, 100);
        spAnimMs = makeSpinner(config.getAnimationStepMs(), 10, 200, 5);

        p.add(new JLabel("Llegada de clientes (ms):")); p.add(spArrivalMs);
        p.add(new JLabel("Atencion recepcion (ms):")); p.add(spReceptionMs);
        p.add(new JLabel("Cotizacion vendedor (ms):")); p.add(spQuoteMs);
        p.add(new JLabel("Impresion en B2 (ms):")); p.add(spPrinterMs);
        p.add(new JLabel("Revision supervisor (ms):")); p.add(spSupervisorMs);
        p.add(new JLabel("Cierre administrador (ms):")); p.add(spAdminMs);
        p.add(new JLabel("Duracion descanso B7 (ms):")); p.add(spRestMs);
        p.add(new JLabel("Paso animacion (ms):")); p.add(spAnimMs);

        return p;
    }

    private JPanel buildBuffersPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("3. Secciones Criticas y Buferes (m de n, donde 1 <= m <= n)"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;

        String[] headers = {"Seccion Critica / Bufer", "Tipo", "m (Actual)", " ", "n (Maximo)", "Validacion"};
        for (int col = 0; col < headers.length; col++) {
            gbc.gridx = col;
            JLabel h = new JLabel(headers[col]);
            h.setFont(h.getFont().deriveFont(Font.BOLD));
            p.add(h, gbc);
        }

        BufferConfig[] all = {
                config.getB1ReceptionDesk(),
                config.getB2Printer(),
                config.getB3ReceptionQueue(),
                config.getB4RequestTray(),
                config.getB5ReviewTray(),
                config.getB6ResultTray(),
                config.getB7RestZone()
        };

        for (BufferConfig bc : all) {
            gbc.gridy++;
            JSpinner spM = makeSpinner(bc.getM(), 1, 50, 1);
            JSpinner spN = makeSpinner(bc.getN(), 1, 50, 1);
            JLabel lblStatus = new JLabel();
            BufferRow row = new BufferRow(bc, spM, spN, lblStatus);
            bufferRows.add(row);

            Runnable validate = () -> {
                int m = (Integer) spM.getValue();
                int n = (Integer) spN.getValue();
                if (m <= n) {
                    lblStatus.setText("OK (" + m + " <= " + n + ")");
                    lblStatus.setForeground(new Color(22, 128, 57));
                } else {
                    lblStatus.setText("Error: m > n");
                    lblStatus.setForeground(Color.RED);
                }
            };
            spM.addChangeListener(e -> validate.run());
            spN.addChangeListener(e -> {
                int n = (Integer) spN.getValue();
                if ((Integer) spM.getValue() > n) {
                    spM.setValue(n);
                }
                validate.run();
            });
            validate.run();

            gbc.gridx = 0; p.add(new JLabel(bc.getCode() + ". " + bc.getName()), gbc);
            gbc.gridx = 1; p.add(new JLabel(bc.getKind()), gbc);
            gbc.gridx = 2; p.add(spM, gbc);
            gbc.gridx = 3; p.add(new JLabel("de"), gbc);
            gbc.gridx = 4; p.add(spN, gbc);
            gbc.gridx = 5; p.add(lblStatus, gbc);
        }

        return p;
    }

    private JPanel buildDistributedPanel() {
        JPanel p = new JPanel(new GridLayout(0, 4, 10, 6));
        p.setBorder(BorderFactory.createTitledBorder("4. Aspecto Distribuido (Agente Sucursal / Matriz por Sockets)"));

        cbMode = new JComboBox<>(SimulationConfig.DistributedMode.values());
        cbMode.setSelectedItem(config.getDistributedMode());
        tfHost = new JTextField(config.getHost());
        spPort = makeSpinner(config.getPort(), 1024, 65535, 1);

        p.add(new JLabel("Modo de sede:")); p.add(cbMode);
        p.add(new JLabel("Host Matriz:")); p.add(tfHost);
        p.add(new JLabel("Puerto Socket:")); p.add(spPort);
        p.add(new JLabel("")); p.add(new JLabel(""));

        return p;
    }

    private JSpinner makeSpinner(int val, int min, int max, int step) {
        int clamped = Math.max(min, Math.min(val, max));
        return new JSpinner(new SpinnerNumberModel(clamped, min, max, step));
    }

    private void applyBasicPreset() {
        spClients.setValue(4);
        spReceptionists.setValue(1);
        spSellers.setValue(0);
        spSupervisors.setValue(0);
        spAdmins.setValue(0);
    }

    private void applyFullPreset() {
        spClients.setValue(6);
        spReceptionists.setValue(1);
        spSellers.setValue(1);
        spSupervisors.setValue(1);
        spAdmins.setValue(1);
    }

    private void onStart() {
        for (BufferRow row : bufferRows) {
            int m = (Integer) row.spM.getValue();
            int n = (Integer) row.spN.getValue();
            if (m < 1 || n < 1 || m > n) {
                JOptionPane.showMessageDialog(this,
                        "En " + row.cfg.getCode() + " (" + row.cfg.getName() + "), m debe cumplir 1 <= m <= n.",
                        "Capacidad invalida",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            row.cfg.setN(n);
            row.cfg.setM(m);
        }

        config.setClientCount((Integer) spClients.getValue());
        config.setReceptionistCount((Integer) spReceptionists.getValue());
        config.setSellerCount((Integer) spSellers.getValue());
        config.setSupervisorCount((Integer) spSupervisors.getValue());
        config.setAdminCount((Integer) spAdmins.getValue());
        config.setSupervisorUnitLimit((Integer) spUnitLimit.getValue());
        config.setOrdersBeforeRest((Integer) spOrdersRest.getValue());

        config.setClientArrivalIntervalMs((Integer) spArrivalMs.getValue());
        config.setReceptionTimeMs((Integer) spReceptionMs.getValue());
        config.setSellerQuoteTimeMs((Integer) spQuoteMs.getValue());
        config.setPrinterTimeMs((Integer) spPrinterMs.getValue());
        config.setSupervisorReviewTimeMs((Integer) spSupervisorMs.getValue());
        config.setAdminProcessTimeMs((Integer) spAdminMs.getValue());
        config.setRestDurationMs((Integer) spRestMs.getValue());
        config.setAnimationStepMs((Integer) spAnimMs.getValue());

        config.setDistributedMode((SimulationConfig.DistributedMode) cbMode.getSelectedItem());
        config.setHost(tfHost.getText());
        config.setPort((Integer) spPort.getValue());

        DashboardFrame dashboard = new DashboardFrame(config);
        dashboard.setVisible(true);
        dispose();
    }
}
