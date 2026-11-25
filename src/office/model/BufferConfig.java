package office.model;

/**
 * Configuracion de capacidad (m de n) para un bufer o zona critica.
 * m es la capacidad activa configurada; n es la capacidad maxima permitida (1 <= m <= n).
 */
public class BufferConfig {
    private final String code;
    private final String name;
    private final String kind;
    private int m;
    private int n;

    public BufferConfig(String code, String name, String kind, int m, int n) {
        this.code = code;
        this.name = name;
        this.kind = kind;
        this.n = Math.max(1, n);
        this.m = Math.max(1, Math.min(m, this.n));
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public int getM() {
        return m;
    }

    public void setM(int m) {
        this.m = Math.max(1, Math.min(m, n));
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = Math.max(1, n);
        if (this.m > this.n) {
            this.m = this.n;
        }
    }

    public boolean isValid() {
        return n >= 1 && m >= 1 && m <= n;
    }
}
