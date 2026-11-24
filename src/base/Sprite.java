
package base;

public class Sprite {
    public String sprite;
    public float x, y, order;
    public final int index;

    public Sprite(String sprite, float x, float y, float order, int index) {
        this.sprite = sprite;
        this.x = x;
        this.y = y;
        this.order = order;
        this.index = index;
    }

    @Override
    public String toString() {
        return "Sprite(" + toSubmissionFormat(this) + ")";
    }

    public static String toSubmissionFormat(Sprite sprite) {
        // sprite, x, y, zorder, index
        return sprite.sprite + "//"
                + sprite.x + "//"
                + sprite.y + "//"
                + sprite.order + "//"
                + sprite.index;
    }

    public static Sprite fromSubmissionFormat(String format) {
        if (format == null) {
            return null;
        }

        String[] split = format.split("//", -1);

        if (split.length != 5 || split[0].isBlank()) {
            return null;
        }

        try {
            return new Sprite(
                    split[0],
                    Float.parseFloat(split[1]),
                    Float.parseFloat(split[2]),
                    Float.parseFloat(split[3]),
                    Integer.parseInt(split[4])
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
