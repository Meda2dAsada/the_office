import base.Client;
import simulacion.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class VentanaSimulador extends JFrame {
    private final PanelConfiguracion configuracion = new PanelConfiguracion();
    private final OficinaTheOffice plano = OficinaTheOffice.crearPanel();
    private final JButton iniciar = new JButton("Iniciar");
    private final JButton detener = new JButton("Detener");
    private final JButton configurar = new JButton("Configuracion");
    private final JButton tableros = new JButton("Ver tableros");
    private final JLabel resumen = new JLabel("  Primero configura los parametros de la simulacion.");
    private Parametros parametros;
    private Simulacion simulacion;
    private VentanaEstados estados;
    private VentanaRecursos recursos;
    private VentanaBPMN procesos;

    public VentanaSimulador(Client conexion) {
        super("Simulador de oficina");

        JCheckBox cuadricula = new JCheckBox("Cuadricula");
        JLabel estadoConexion = new JLabel(conexion.getEstadoConexion());

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barra.add(iniciar);
        barra.add(detener);
        barra.add(configurar);
        barra.add(tableros);
        barra.add(cuadricula);
        barra.add(estadoConexion);

        add(barra, BorderLayout.NORTH);
        add(plano, BorderLayout.CENTER);
        add(resumen, BorderLayout.SOUTH);

        configurar.addActionListener(e -> configurarSimulacion());
        iniciar.addActionListener(e -> iniciarSimulacion());
        tableros.addActionListener(e -> mostrarTableros());

        cuadricula.addActionListener(e -> plano.setMostrarCuadricula(cuadricula.isSelected()));

        detener.addActionListener(e -> {
            if (simulacion != null) {
                simulacion.detener();
            }
            actualizarBotones();
        });

        Timer reloj = new Timer(200, e -> {
            actualizarBotones();
            estadoConexion.setText(conexion.getEstadoConexion());

            if (simulacion != null) {
                actualizarTableros();

                resumen.setText(
                        "  Rojo: recepcionistas | Azul: clientes"
                                + " | Solicitudes registradas: "
                                + simulacion.getRecepcion().getDatos()[3]
                                + " / " + simulacion.getParametros().getClientes());
            }
        });

        Timer animacion = new Timer(30, e -> plano.repaint());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                reloj.stop();
                animacion.stop();

                if (simulacion != null) simulacion.detener();
                if (estados != null) estados.dispose();
                if (recursos != null) recursos.dispose();
                if (procesos != null) procesos.dispose();
            }
        });

        actualizarBotones();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setMinimumSize(new Dimension(1000, 580));
        setLocationRelativeTo(null);
        setVisible(true);

        reloj.start();
        animacion.start();
    }

    private void configurarSimulacion() {
        if (simulacion != null && simulacion.isActiva()) {
            return;
        }

        while (JOptionPane.showConfirmDialog(this, configuracion, "Configuracion", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                parametros = configuracion.leer();
                resumen.setText("  Configuracion guardada. Pulsa Iniciar.");
                actualizarBotones();
                return;
            } catch (IllegalArgumentException error) {
                JOptionPane.showMessageDialog(this, error.getMessage());
            }
        }
    }

    private void iniciarSimulacion() {
        if (parametros == null) return;
        if (simulacion != null && simulacion.isActiva()) {
            return;
        }

        simulacion = new Simulacion(parametros);
        plano.setSimulacion(simulacion);

        if (estados == null) {
            estados = new VentanaEstados();
            recursos = new VentanaRecursos();
            procesos = new VentanaBPMN();
        }

        simulacion.iniciar();
        actualizarTableros();
        mostrarTableros();
        actualizarBotones();
    }

    private void actualizarBotones() {
        boolean activa = simulacion != null && simulacion.isActiva();
        iniciar.setEnabled(parametros != null && !activa);
        detener.setEnabled(activa);
        configurar.setEnabled(!activa);
        tableros.setEnabled(estados != null);
    }

    private void actualizarTableros() {
        if (simulacion == null || estados == null) {
            return;
        }
        estados.actualizar(simulacion);
        recursos.actualizar(simulacion);
        procesos.actualizar(simulacion);
    }

    private void mostrarTableros() {
        if (estados == null) return;
        for (JFrame ventana : new JFrame[]{estados, recursos, procesos}) {
            ventana.setVisible(true);
            ventana.setState(JFrame.NORMAL);
            ventana.toFront();
        }
    }
}