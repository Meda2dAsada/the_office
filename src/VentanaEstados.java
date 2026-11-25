import simulacion.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class VentanaEstados extends JFrame {
    private final DefaultTableModel modelo = new DefaultTableModel(new String[]{"Estado Java", "Clientes", "Recepcionistas", "Total"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    public VentanaEstados() {
        super("Inciso c) Estados de los hilos");

        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(24);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);

        add(new JScrollPane(tabla));

        setSize(750, 330);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationByPlatform(true);
    }

    public void actualizar(Simulacion simulacion) {
        int[][] cantidades = new int[2][Thread.State.values().length];

        for (AgenteOficina agente : simulacion.getAgentes()) {
            int tipo;

            if (agente.getTipo().equals("Cliente")) tipo = 0;
            else if (agente.getTipo().equals("Recepcionista")) tipo = 1;
            else continue;

            cantidades[tipo][agente.getState().ordinal()]++;
        }

        modelo.setRowCount(0);

        for (Thread.State estado : Thread.State.values()) {
            int clientes = cantidades[0][estado.ordinal()];
            int recepcionistas = cantidades[1][estado.ordinal()];

            modelo.addRow(new Object[]{
                    estado, clientes, recepcionistas, clientes + recepcionistas
            });
        }
    }
}