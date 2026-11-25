import office.model.SimulationConfig;
import office.ui.ConfigFrame;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada para la computadora remota (Sucursal / Agente Cliente Socket):
 * abre la configuracion preseleccionada en modo Sucursal para conectarse a la Matriz.
 */
public class MainClient {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimulationConfig config = new SimulationConfig();
            config.setDistributedMode(SimulationConfig.DistributedMode.SUCURSAL_CLIENTE);
            new ConfigFrame(config).setVisible(true);
        });
    }
}
