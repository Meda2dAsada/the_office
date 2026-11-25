import simulacion.Parametros;
import javax.swing.*;
import java.awt.*;
import java.text.ParseException;

public class PanelConfiguracion extends JPanel {
    private final JSpinner clientes = numero(8, 1, 100);
    private final JSpinner recepcionistas = numero(2, 1, 10);
    private final JSpinner[] tiempos = new JSpinner[7];
    private final JSpinner[] capacidades = new JSpinner[7];

    public PanelConfiguracion() {
        setLayout(new GridLayout(0, 3, 6, 6));
        setBorder(BorderFactory.createTitledBorder("Parametros para el siguiente inicio"));

        agregar("Clientes", clientes, "1 a 100");
        agregar("Recepcionistas", recepcionistas, "1 a 10");

        int[] valores = {400, 1200, 800, 1500, 1000, 700, 2000};

        for (int i = 0; i < tiempos.length; i++) {
            tiempos[i] = numero(valores[i], 0, 30000);
            agregar(Parametros.TIEMPOS[i], tiempos[i], "ms");
        }

        for (int i = 0; i < capacidades.length; i++) {
            capacidades[i] = numero(
                    Parametros.MAXIMOS[i], 1, Parametros.MAXIMOS[i]);

            agregar(Parametros.RECURSOS[i], capacidades[i],
                    "de " + Parametros.MAXIMOS[i]);
        }

        add(new JLabel("Activos: B1 y B3"));
        add(new JLabel("Otros: pendientes"));
        add(new JLabel("m <= n"));
    }

    private static JSpinner numero(int valor, int minimo, int maximo) {
        return new JSpinner(
                new SpinnerNumberModel(valor, minimo, maximo, 1));
    }

    private void agregar(String texto, JSpinner campo, String unidad) {
        add(new JLabel(texto));
        add(campo);
        add(new JLabel(unidad));
    }

    public Parametros leer() {
        try {
            clientes.commitEdit();
            recepcionistas.commitEdit();

            for (JSpinner campo : tiempos) {
                campo.commitEdit();
            }

            for (JSpinner campo : capacidades) {
                campo.commitEdit();
            }

        } catch (ParseException e) {
            throw new IllegalArgumentException("Revisa los valores de la configuracion");
        }

        int[] t = new int[tiempos.length];
        int[] c = new int[capacidades.length];

        for (int i = 0; i < t.length; i++) {
            t[i] = (Integer) tiempos[i].getValue();
        }

        for (int i = 0; i < c.length; i++) {
            c[i] = (Integer) capacidades[i].getValue();
        }

        return new Parametros((Integer) clientes.getValue(), (Integer) recepcionistas.getValue(), t, c);
    }
}