package base;

public abstract class Agent extends SceneObject {
    public Agent(Sprite sprite) {
        super(sprite);
    }

    @Override
    public synchronized void run() {
        work();
        super.run();
    }

    public abstract void work();
}
