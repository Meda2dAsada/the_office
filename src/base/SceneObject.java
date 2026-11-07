package base;

public class SceneObject extends Thread {
    public static Server SERVER = null;
    private boolean needsRepaint;
    private Sprite sprite;

    public SceneObject(Sprite sprite) {
        setSprite(sprite);
        setNeedsRepaint(true);
    }

    protected void updateRender() {
        Sprite currentSprite = getSprite();
        if (currentSprite != null && SERVER != null) {
            //SERVER.update(currentSprite); // manda la informacion al server para actualizar todas las pantallas conectadas
            setNeedsRepaint(false);
        }

    }

    private void setSprite(Sprite sprite) {this.sprite = sprite;}
    public Sprite getSprite() {return sprite;}

    @Override
    public synchronized void run() {
        while(true) {
            if(getNeedsRepaint()) updateRender();
        }
    }

    public boolean getNeedsRepaint() {return needsRepaint;}
    public void setNeedsRepaint(boolean needsRepaint) {this.needsRepaint = needsRepaint;}

}
