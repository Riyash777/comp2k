import java.awt.Graphics;
import java.util.ArrayList;

public class LightManager {

    private ArrayList<Lights> lights = new ArrayList<>();

    private int state;
    private double elapsedTime;

    public LightManager() {
        lights.add(new Lights(290, 300, 10, 200));   // Left
        lights.add(new Lights(500, 300, 10, 200));   // Right
        lights.add(new Lights(300, 290, 200, 10));   // Top
        lights.add(new Lights(300, 500, 200, 10));   // Bottom

        state = 1;
        elapsedTime = 0;

        setLights();
    }

    public void update(double deltaTime) {
        elapsedTime += deltaTime;

        if (state == 1 && elapsedTime >= 8) {
            state = 2;
            elapsedTime = 0;
        } else if (state == 2 && elapsedTime >= 3) {
            state = 3;
            elapsedTime = 0;
        } else if (state == 3 && elapsedTime >= 3) {
            state = 4;
            elapsedTime = 0;
        } else if (state == 4 && elapsedTime >= 8) {
            state = 5;
            elapsedTime = 0;
        } else if (state == 5 && elapsedTime >= 3) {
            state = 6;
            elapsedTime = 0;
        } else if (state == 6 && elapsedTime >= 3) {
            state = 1;
            elapsedTime = 0;
        }

        setLights();
    }

    private void setLights() {
        Lights lightLeft = lights.get(0);
        Lights lightRight = lights.get(1);
        Lights lightTop = lights.get(2);
        Lights lightBottom = lights.get(3);

        try {
            switch (state) {
                case 1:
                    lightLeft.changeLight(1);
                    lightRight.changeLight(1);
                    lightTop.changeLight(3);
                    lightBottom.changeLight(3);
                    break;

                case 2:
                    lightLeft.changeLight(2);
                    lightRight.changeLight(2);
                    lightTop.changeLight(3);
                    lightBottom.changeLight(3);
                    break;

                case 3:
                    setAllRed();
                    break;

                case 4:
                    lightLeft.changeLight(3);
                    lightRight.changeLight(3);
                    lightTop.changeLight(1);
                    lightBottom.changeLight(1);
                    break;

                case 5:
                    lightLeft.changeLight(3);
                    lightRight.changeLight(3);
                    lightTop.changeLight(2);
                    lightBottom.changeLight(2);
                    break;

                case 6:
                    setAllRed();
                    break;
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Traffic light error: " + e.getMessage());
        }
    }

    private void setAllRed() {
        for (Lights light : lights) {
            light.changeLight(3);
        }
    }

    public void draw(Graphics g) {
        for (Lights light : lights) {
            light.spawnLight(g);
        }
    }

    public int getState() {
        return state;
    }

    public Lights getLightLeft() {
        return lights.get(0);
    }

    public Lights getLightRight() {
        return lights.get(1);
    }

    public Lights getLightTop() {
        return lights.get(2);
    }

    public Lights getLightBottom() {
        return lights.get(3);
    }
}
