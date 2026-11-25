import simulacion.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VentanaRecursos extends JFrame {
    private final DefaultTableModel agentes = crearModelo("Tipo de agente", "Cantidad total");

    private final DefaultTableModel recursos = crearModelo("Recurso", "Capacidad m / n", "Clientes dentro", "Clientes esperando", "Recepcionistas esperando");

    private final JLabel registros = new JLabel();

    public VentanaRecursos() {
        super("Inciso d) Cantidades y recursos");

        JPanel centro = new JPanel(new GridLayout(2, 1, 8, 8));
        centro.add(crearTabla(agentes, "Agentes creados"));
        centro.add(crearTabla(recursos, "Ocupacion y espera en B1 y B3"));

        JPanel pie = new JPanel(new GridLayout(2, 1));
        pie.add(registros);
        pie.add(new JLabel("  Recepcionistas: en B1 esperan el puesto; en B3 esperan un cliente."));

        add(centro, BorderLayout.CENTER);
        add(pie, BorderLayout.SOUTH);

        setSize(1000, 450);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationByPlatform(true);
    }

    private static DefaultTableModel crearModelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    private JPanel crearTabla(DefaultTableModel modelo, String titulo) {
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(24);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(titulo));
        panel.add(new JScrollPane(tabla));

        return panel;
    }

    public void actualizar(Simulacion simulacion) {
        int clientes = 0, recepcionistas = 0;
        int esperandoB1 = 0, esperandoClientes = 0;

        for (AgenteOficina agente : simulacion.getAgentes()) {
            if (agente.getTipo().equals("Cliente")) clientes++;

            if (agente.getTipo().equals("Recepcionista")) {
                recepcionistas++;

                String actividad = agente.getEstadoBpmn();

                if (actividad.equals("Esperar B1 libre"))
                    esperandoB1++;

                if (actividad.equals("Esperar cliente en B3"))
                    esperandoClientes++;
            }
        }

        agentes.setRowCount(0);
        agentes.addRow(new Object[]{"Cliente", clientes});
        agentes.addRow(new Object[]{"Recepcionista", recepcionistas});

        int[] datos = simulacion.getRecepcion().getDatos();
        Parametros parametros = simulacion.getParametros();

        recursos.setRowCount(0);

        recursos.addRow(new Object[]{
                "B1 Atencion", parametros.getCapacidad(0) + " / 1",
                datos[0], datos[1], esperandoB1
        });

        recursos.addRow(new Object[]{
                "B3 Fila", parametros.getCapacidad(2) + " / 3",
                datos[1], datos[2], esperandoClientes
        });

        registros.setText("  Solicitudes registradas: " + datos[3]);
    }
}