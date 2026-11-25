import office.model.SimulationConfig;
import office.ui.ConfigFrame;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada principal (Matriz / Servidor):
 * abre la ventana de configuracion de entradas y lanza el simulador "The Office WTC".
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimulationConfig config = new SimulationConfig();
            config.setDistributedMode(SimulationConfig.DistributedMode.MATRIZ_SERVIDOR);
            new ConfigFrame(config).setVisible(true);
        });
    }
}
