import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VehicleManager {

    private static final int CAR_SPAWN_INTERVAL_MS = 1500;
    private static final float STOP_LINE_BUFFER = 10f;
    private static final float BASE_GAP = 20f;
    private static final float ANGLE_EPSILON = 0.01f;

    private final List<Car> vehicles;
    private final LightManager trafficLights;
    private final Random random;

    private double lastCarSpawnTime = 0;

    public VehicleManager(LightManager trafficLights) {
        this.trafficLights = trafficLights;
        this.vehicles = new ArrayList<>();
        this.random = new Random();

        vehicles.add(new Car(0, 340, 30f, 50f, 0f, Color.MAGENTA));
        vehicles.add(new Car(450, 0, 20f, 30f,
                (float) (Math.PI / 2), Color.MAGENTA));
        vehicles.add(new Car(740, 440, 10f, 40f,
                (float) Math.PI, Color.MAGENTA));
    }

    public void update(double deltaTime, double worldTimer) {

        for (Car car : vehicles) {
            if (shouldStopFor2(car) || tooCloseToCarAhead(car)) {
                car.slowDown(deltaTime);
            } else {
                car.speedUp(deltaTime);
            }

            car.move(deltaTime);
        }

        if (worldTimer - lastCarSpawnTime >= CAR_SPAWN_INTERVAL_MS) {
            spawnCar(worldTimer);
        }

        checkCollisions();
    }

    private void spawnCar(double worldTimer) {
        int randomNumber = random.nextInt(4) + 1;
        Car newCar = null;

        switch (randomNumber) {
            case 1:
                newCar = new Car(0, 340, 20, 50, 0, Color.CYAN);
                break;

            case 2:
                newCar = new Car(450, 0, 20, 50,
                        (float) (Math.PI / 2), Color.CYAN);
                break;

            case 3:
                newCar = new Car(350, 740, 20, 50,
                        (float) (3 * Math.PI / 2), Color.CYAN);
                break;

            case 4:
                newCar = new Car(740, 440, 20, 50,
                        (float) Math.PI, Color.CYAN);
                break;
        }

        if (newCar != null) {
            vehicles.add(newCar);
        }

        lastCarSpawnTime = worldTimer;
    }

    public void draw(Graphics g) {
        for (Car car : vehicles) {
            car.draw(g);
        }
    }

    public void addVehicle(Car vehicle) {
        if (vehicle != null) {
            vehicles.add(vehicle);
        }
    }

    public void removeVehicle(Car vehicle) {
        vehicles.remove(vehicle);
    }

    public List<Car> getVehicles() {
        return vehicles;
    }

    public boolean isCollidingWithCar(Car car1, Car car2) {
        float[] pos1 = car1.getPosition();
        float[] pos2 = car2.getPosition();

        double distance = getDistance(
                pos1[0], pos1[1], pos2[0], pos2[1]
        );

        return distance < car1.getRadius() + car2.getRadius();
    }

    private void checkCollisions() {
        for (int i = 0; i < vehicles.size(); i++) {
            for (int j = i + 1; j < vehicles.size(); j++) {
                Car car1 = vehicles.get(i);
                Car car2 = vehicles.get(j);

                if (isCollidingWithCar(car1, car2)) {
                    handleCollision(car1, car2);
                }
            }
        }
    }

    private void handleCollision(Car car1, Car car2) {
        float[] pos1 = car1.getPosition();
        float[] pos2 = car2.getPosition();

        float dx = pos2[0] - pos1[0];
        float dy = pos2[1] - pos1[1];

        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance == 0f) {
            distance = 0.01f;
        }

        float nx = dx / distance;
        float ny = dy / distance;

        float relVx = car1.getVelocityX() - car2.getVelocityX();
        float relVy = car1.getVelocityY() - car2.getVelocityY();
        float velAlongNormal = relVx * nx + relVy * ny;

        if (velAlongNormal < 0) {
            return;
        }

        float restitution = 0.4f;

        float impulse = -(1 + restitution) * velAlongNormal
                / (1f / car1.getMass() + 1f / car2.getMass());

        float impulseX = impulse * nx;
        float impulseY = impulse * ny;

        car1.setVelocity(
                car1.getVelocityX() + impulseX / car1.getMass(),
                car1.getVelocityY() + impulseY / car1.getMass()
        );

        car2.setVelocity(
                car2.getVelocityX() - impulseX / car2.getMass(),
                car2.getVelocityY() - impulseY / car2.getMass()
        );

        separateOverlap(car1, car2, nx, ny, distance);
    }

    private void separateOverlap(Car a, Car b,
                                  float nx, float ny, float distance) {
        float overlap = (a.getRadius() + b.getRadius()) - distance;

        if (overlap <= 0) {
            return;
        }

        float totalMass = a.getMass() + b.getMass();

        float pushA = overlap * (b.getMass() / totalMass);
        float pushB = overlap * (a.getMass() / totalMass);

        float[] posA = a.getPosition();
        float[] posB = b.getPosition();

        a.setPosition(
                posA[0] - nx * pushA,
                posA[1] - ny * pushA
        );

        b.setPosition(
                posB[0] + nx * pushB,
                posB[1] + ny * pushB
        );
    }

    public boolean shouldStopFor2(Car car) {
        Lights light = getGoverningLight(car);

        if (light == null || !light.isRed()) {
            return false;
        }

        float distance = distanceToStopLine(car, light);

        if (distance < 0) {
            return false;
        }

        float brakingDistance = getStoppingDistance(car) + STOP_LINE_BUFFER;

        return distance <= brakingDistance;
    }

    private Lights getGoverningLight(Car car) {
        float dir = normalizeAngle(car.getDirectionRadians());

        if (Math.abs(dir - 0) < ANGLE_EPSILON) {
            return trafficLights.getLightLeft();
        }

        if (Math.abs(dir - (float) (Math.PI / 2)) < ANGLE_EPSILON) {
            return trafficLights.getLightTop();
        }

        if (Math.abs(dir - (float) Math.PI) < ANGLE_EPSILON) {
            return trafficLights.getLightRight();
        }

        if (Math.abs(dir - (float) (3 * Math.PI / 2)) < ANGLE_EPSILON) {
            return trafficLights.getLightBottom();
        }

        return null;
    }

    private float distanceToStopLine(Car car, Lights light) {
        float[] pos = car.getPosition();
        float dir = normalizeAngle(car.getDirectionRadians());

        if (Math.abs(dir - 0) < ANGLE_EPSILON) {
            return light.getX() - (pos[0] + car.getRadius());
        }

        if (Math.abs(dir - (float) (Math.PI / 2)) < ANGLE_EPSILON) {
            return light.getY() - (pos[1] + car.getRadius());
        }

        if (Math.abs(dir - (float) Math.PI) < ANGLE_EPSILON) {
            return (pos[0] - car.getRadius())
                    - (light.getX() + light.getWidth());
        }

        return (pos[1] - car.getRadius())
                - (light.getY() + light.getHeight());
    }

    private float normalizeAngle(float angle) {
        float twoPi = (float) (2 * Math.PI);
        return ((angle % twoPi) + twoPi) % twoPi;
    }

    private Car findCarAhead(Car car) {
        float[] pos = car.getPosition();
        float dir = normalizeAngle(car.getDirectionRadians());

        Car closest = null;
        float closestDistance = Float.MAX_VALUE;

        for (Car other : vehicles) {
            if (other == car) {
                continue;
            }

            float otherDir = normalizeAngle(other.getDirectionRadians());
            float angleDifference = Math.abs(otherDir - dir);

            if (angleDifference > ANGLE_EPSILON) {
                continue;
            }

            float[] otherPos = other.getPosition();

            float dx = otherPos[0] - pos[0];
            float dy = otherPos[1] - pos[1];

            float forwardDistance =
                    dx * (float) Math.cos(dir)
                            + dy * (float) Math.sin(dir);

            if (forwardDistance > 0
                    && forwardDistance < closestDistance) {
                closestDistance = forwardDistance;
                closest = other;
            }
        }

        return closest;
    }

    private boolean tooCloseToCarAhead(Car car) {
        Car ahead = findCarAhead(car);

        if (ahead == null) {
            return false;
        }

        double distance = getDistance(
                car.getPosition()[0],
                car.getPosition()[1],
                ahead.getPosition()[0],
                ahead.getPosition()[1]
        );

        float gap = (float) distance
                - car.getRadius()
                - ahead.getRadius();

        float desiredGap = getStoppingDistance(car) + BASE_GAP;

        return gap < desiredGap;
    }

    public float getStoppingDistance(Car car) {
        float speed = car.getSpeed();
        float acceleration = car.getAcceleration();

        if (acceleration <= 0) {
            return Float.MAX_VALUE;
        }

        return (speed * speed) / (2 * acceleration);
    }

    public static double getDistance(double x1, double y1,
                                     double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;

        return Math.hypot(dx, dy);
    }
}
