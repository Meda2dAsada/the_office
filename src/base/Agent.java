package base;

public abstract class Agent extends SceneObject {
    public Agent(Sprite sprite) {
        super(sprite);
    }

    @Override
    public final void run() {
        work();

    }

    public abstract void work();
}
