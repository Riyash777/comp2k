import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class PedestrianManager {

    private static final int PEDESTRIAN_SPAWN_INTERVAL_MS = 2000;
    private static final float WALK_SPEED = 100f;
    private static final float POSITION_TOLERANCE = 3f;

    private final List<Pedestrian> pedestrians;
    private final LightManager trafficLights;
    private final VehicleManager vehicleManager;
    private final Random random;

    private double lastPedestrianSpawnTime = 0;

    public PedestrianManager(LightManager trafficLights,
                             VehicleManager vehicleManager) {
        this.trafficLights = trafficLights;
        this.vehicleManager = vehicleManager;
        this.pedestrians = new ArrayList<>();
        this.random = new Random();
    }

    public void update(double deltaTime, double worldTimer) {

        if (worldTimer - lastPedestrianSpawnTime >= PEDESTRIAN_SPAWN_INTERVAL_MS) {
            spawnPedestrian(worldTimer);
        }

        for (Pedestrian pedestrian : pedestrians) {
            if (pedestrian.isHit()) {
                continue;
            }

            handleCrossing(pedestrian);
            pedestrian.update(deltaTime);
            checkCarCollision(pedestrian);
        }

        Iterator<Pedestrian> iterator = pedestrians.iterator();

        while (iterator.hasNext()) {
            if (iterator.next().shouldRemove()) {
                iterator.remove();
            }
        }
    }

    private void spawnPedestrian(double worldTimer) {
        int side = random.nextInt(4);
        Pedestrian pedestrian = null;

        switch (side) {
            case 0:
                pedestrian = new Pedestrian(
                        800, 295, 7, WALK_SPEED, (float) Math.PI
                );
                break;

            case 1:
                pedestrian = new Pedestrian(
                        0, 505, 7, WALK_SPEED, 0
                );
                break;

            case 2:
                pedestrian = new Pedestrian(
                        295, 800, 7, WALK_SPEED, (float) (3 * Math.PI / 2)
                );
                break;

            case 3:
                pedestrian = new Pedestrian(
                        505, 0, 7, WALK_SPEED, (float) (Math.PI / 2)
                );
                break;
        }

        pedestrians.add(pedestrian);
        lastPedestrianSpawnTime = worldTimer;
    }

    private void handleCrossing(Pedestrian pedestrian) {
        float x = pedestrian.getX();
        float y = pedestrian.getY();

        if (Math.abs(y - 295) < POSITION_TOLERANCE) {
            if (pedestrian.isCrossing()) {
                if (x <= 300) {
                    pedestrian.setCrossing(false);
                    pedestrian.setWaiting(false);
                    pedestrian.setSpeed(WALK_SPEED);
                }
                return;
            }

            if (x <= 500 && x > 480) {
                if (verticalRoadIsRed()) {
                    pedestrian.setCrossing(true);
                    pedestrian.setWaiting(false);
                    pedestrian.setSpeed(WALK_SPEED);
                } else {
                    pedestrian.setWaiting(true);
                    pedestrian.setSpeed(0);
                }
                return;
            }
        }

        if (Math.abs(y - 505) < POSITION_TOLERANCE) {
            if (pedestrian.isCrossing()) {
                if (x >= 500) {
                    pedestrian.setCrossing(false);
                    pedestrian.setWaiting(false);
                    pedestrian.setSpeed(WALK_SPEED);
                }
                return;
            }

            if (x >= 300 && x < 320) {
                if (verticalRoadIsRed()) {
                    pedestrian.setCrossing(true);
                    pedestrian.setWaiting(false);
                    pedestrian.setSpeed(WALK_SPEED);
                } else {
                    pedestrian.setWaiting(true);
                    pedestrian.setSpeed(0);
                }
                return;
            }
        }

        if (Math.abs(x - 295) < POSITION_TOLERANCE) {
            if (pedestrian.isCrossing()) {
                if (y <= 300) {
                    pedestrian.setCrossing(false);
                    pedestrian.setWaiting(false);
                    pedestrian.setSpeed(WALK_SPEED);
                }
                return;
            }

            if (y <= 500 && y > 480) {
                if (horizontalRoadIsRed()) {
                    pedestrian.setCrossing(true);
                    pedestrian.setWaiting(false);
                    pedestrian.setSpeed(WALK_SPEED);
                } else {
                    pedestrian.setWaiting(true);
                    pedestrian.setSpeed(0);
                }
                return;
            }
        }

        if (Math.abs(x - 505) < POSITION_TOLERANCE) {
            if (pedestrian.isCrossing()) {
                if (y >= 500) {
                    pedestrian.setCrossing(false);
                    pedestrian.setWaiting(false);
                    pedestrian.setSpeed(WALK_SPEED);
                }
                return;
            }

            if (y >= 300 && y < 320) {
                if (horizontalRoadIsRed()) {
                    pedestrian.setCrossing(true);
                    pedestrian.setWaiting(false);
                    pedestrian.setSpeed(WALK_SPEED);
                } else {
                    pedestrian.setWaiting(true);
                    pedestrian.setSpeed(0);
                }
                return;
            }
        }

        if (!pedestrian.isCrossing() && !pedestrian.isWaiting()) {
            pedestrian.setSpeed(WALK_SPEED);
        }
    }

    private boolean verticalRoadIsRed() {
        return trafficLights.getLightTop().isRed()
                && trafficLights.getLightBottom().isRed();
    }

    private boolean horizontalRoadIsRed() {
        return trafficLights.getLightLeft().isRed()
                && trafficLights.getLightRight().isRed();
    }

    private void checkCarCollision(Pedestrian pedestrian) {
        for (Car car : vehicleManager.getVehicles()) {
            float[] carPosition = car.getPosition();

            double distance = VehicleManager.getDistance(
                    pedestrian.getX(),
                    pedestrian.getY(),
                    carPosition[0],
                    carPosition[1]
            );

            double collisionDistance =
                    pedestrian.getRadius() + car.getRadius();

            if (distance <= collisionDistance) {
                pedestrian.hit();
                return;
            }
        }
    }

    public void draw(Graphics g) {
        for (Pedestrian pedestrian : pedestrians) {
            pedestrian.draw(g);
        }
    }

    public List<Pedestrian> getPedestrians() {
        return pedestrians;
    }
}
