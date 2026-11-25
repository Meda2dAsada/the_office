import simulacion.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class VentanaBPMN extends JFrame {
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Agente", "Tipo", "Actividad actual (BPMN)"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    public VentanaBPMN() {
        super("Inciso f) Estados del proceso");

        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(24);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);

        tabla.getColumnModel().getColumn(0).setPreferredWidth(180);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(450);

        add(new JScrollPane(tabla));

        setSize(850, 500);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationByPlatform(true);
    }

    public void actualizar(Simulacion simulacion) {
        modelo.setRowCount(0);

        for (AgenteOficina agente : simulacion.getAgentes()) {
            if (agente.getTipo().equals("Cliente")
                    || agente.getTipo().equals("Recepcionista")) {

                modelo.addRow(new Object[]{
                        agente.getName(),
                        agente.getTipo(),
                        agente.getEstadoBpmn()
                });
            }
        }
    }
}