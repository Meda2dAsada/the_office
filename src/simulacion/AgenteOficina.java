package simulacion;

import base.Agent;
import base.Sprite;
import java.awt.*;

public abstract class AgenteOficina extends Agent {
    private final String tipo;
    private volatile String estadoBpmn = "Creado";
    private volatile boolean visible;

    public AgenteOficina(String tipo, int numero, int identificador) {
        super(new Sprite(tipo + "-" + numero, 0, 0, 0, identificador));
        this.tipo = tipo;
        setName(tipo + "-" + numero);
    }

    public String getTipo() {
        return tipo;
    }

    public String getEstadoBpmn() {
        return estadoBpmn;
    }

    public boolean isVisible() {
        return visible;
    }

    protected void mostrar(boolean valor) {
        visible = valor;
    }

    public void cambiarEstado(String estado) {
        estadoBpmn = estado;
    }

    protected void colocar(double x, double y) {
        Sprite sprite = getSprite();
        synchronized (sprite) {
            sprite.x = (float) x;
            sprite.y = (float) y;
        }
    }

    public Point getPosicion() {
        Sprite sprite = getSprite();
        synchronized (sprite) {
            return new Point(
                    Math.round(sprite.x), Math.round(sprite.y));
        }
    }

    protected void caminar(int x, int y, String estado)
            throws InterruptedException {

        cambiarEstado(estado);
        Point inicio = getPosicion();

        double distancia = inicio.distance(x, y);
        int pasos = Math.max(1, (int) Math.ceil(distancia / 6));

        for (int i = 1; i <= pasos; i++) {
            Thread.sleep(25);
            double avance = i / (double) pasos;
            colocar(inicio.x + (x - inicio.x) * avance, inicio.y + (y - inicio.y) * avance);
        }
    }
    protected void pausar(String estado, int milisegundos)
            throws InterruptedException {
        cambiarEstado(estado);
        Thread.sleep(milisegundos);
    }

    @Override
    public final void work() {
        try {
            ejecutar();

            if (isInterrupted()) {
                cambiarEstado("Detenido");
            }

        } catch (InterruptedException e) {
            cambiarEstado("Detenido");
            Thread.currentThread().interrupt();
        }
    }

    protected abstract void ejecutar() throws InterruptedException;
}