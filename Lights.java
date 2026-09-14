import java.awt.Color;
import java.awt.Graphics;

public class Lights extends Objects {

    private int width;
    private int height;
    private int colourMode;

    public Lights(int x, int y, int width, int height) {
        super(x, y);
        this.width = width;
        this.height = height;
        this.colourMode = 1;
    }

    public void spawnLight(Graphics g) {
        if (colourMode == 1) {
            g.setColor(Color.GREEN);
        } else if (colourMode == 2) {
            g.setColor(Color.ORANGE);
        } else {
            g.setColor(Color.RED);
        }

        g.fillRect((int) x, (int) y, width, height);
    }

    public void changeLight(int type) {
        if (type < 1 || type > 3) {
            throw new IllegalArgumentException("Invalid light colour");
        }

        this.colourMode = type;
    }

    public int getColour() {
        return colourMode;
    }

    public boolean isGreen() {
        return colourMode == 1;
    }

    public boolean isYellow() {
        return colourMode == 2;
    }

    public boolean isRed() {
        return colourMode == 3;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
