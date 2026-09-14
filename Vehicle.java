import java.awt.Graphics;

public abstract class Vehicle {

    protected float x;
    protected float y;

    public Vehicle(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public abstract void move(double deltaSeconds);

    public abstract void draw(Graphics g);

    public abstract float getMass();

    public float[] getPosition() {
        return new float[]{x, y};
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
