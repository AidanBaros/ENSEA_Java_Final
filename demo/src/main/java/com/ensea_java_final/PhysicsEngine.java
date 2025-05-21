package com.ensea_java_final;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Enumeration;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PhysicsEngine {
    private ArrayList<Body> bodies = new ArrayList<Body>();
    private Double simulationDeltaT = 0.001;
    private Dictionary<String, Double> gravityDict =  new Hashtable<>();
    private String gravityType = "game";
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private Double timeScale = 1.0; // 1.0 = real time, 0.5 = half speed, 2.0 = double speed
    private Double adjustedDeltaT = simulationDeltaT * timeScale;

    public Double getTimeScale(){ return timeScale;}
    public void setTimeScale(Double timeScale){ this.timeScale = timeScale;}

    
    public PhysicsEngine(ArrayList<Body> bodies){
        this.bodies = bodies;
        gravityDict.put("standard",6.67430e-11);
        gravityDict.put("earth",-9.8);
        gravityDict.put("game",1.0);
    }

    public void setGravityType(String gravityType){
        if (gravityDict.get(gravityType) != null){
            this.gravityType = gravityType;
        } else {
            System.out.println("Invalid gravity type. Defaulting to game gravity.");
            this.gravityType = "game";
        }
    }

    public void update() {
        adjustedDeltaT = simulationDeltaT * timeScale;
        applyGravity(true, gravityDict.get(gravityType));
        moveBodies();
        resolveCollisions(); 
    }
    
    public void resolveCollisions() {
        ArrayList<CollisionPair> collisionPairs = new ArrayList<>();

        for (int i = 0; i < bodies.size(); i++) {
            Body bodyA = bodies.get(i);
            for (int j = i + 1; j < bodies.size(); j++) {
                Body bodyB = bodies.get(j);

                // Broad phase: bounding circle
                double distance = bodyA.getPosition().distance(bodyB.getPosition());
                double collisionDistance = bodyA.getSize() + bodyB.getSize();
                if (distance > collisionDistance) continue;

                // Narrow phase: per-pixel collision if both have alpha masks
                if (bodyA.hasAlphaMask() && bodyB.hasAlphaMask()) {
                    if (!perPixelCollision(bodyA, bodyB)) continue;
                }
                // If only one has alpha mask, check that one
                else if (bodyA.hasAlphaMask()) {
                    if (!perPixelCircleCollision(bodyA, bodyB)) continue;
                }
                else if (bodyB.hasAlphaMask()) {
                    if (!perPixelCircleCollision(bodyB, bodyA)) continue;
                }
                // else: fallback to bounding circle (already passed)

                collisionPairs.add(new CollisionPair(bodyA, bodyB));
            }
        }

        ArrayList<Callable<Void>> tasks = new ArrayList<>();
        for (CollisionPair pair : collisionPairs) {
            tasks.add(() -> {
                resolveCollision(pair.bodyA, pair.bodyB);
                return null;
            });
        }

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Per-pixel collision for two textured bodies
    private boolean perPixelCollision(Body texA, Body texB) {
        // Only works for circles, assumes both are circles
        Vector2D posA = texA.getPosition();
        Vector2D posB = texB.getPosition();
        double rA = texA.getSize();
        double rB = texB.getSize();

        // Find overlap bounding box in world coordinates
        double minX = Math.max(posA.x - rA, posB.x - rB);
        double maxX = Math.min(posA.x + rA, posB.x + rB);
        double minY = Math.max(posA.y - rA, posB.y - rB);
        double maxY = Math.min(posA.y + rA, posB.y + rB);

        // Step size: sample at pixel resolution of the larger texture
        int steps = Math.max(Math.max(texA.getTexWidth(), texB.getTexWidth()), 16);
        double stepX = (maxX - minX) / steps;
        double stepY = (maxY - minY) / steps;

        for (double x = minX; x <= maxX; x += stepX) {
            for (double y = minY; y <= maxY; y += stepY) {
                // Check if point is inside both circles
                if (posA.distance(new Vector2D(x, y)) > rA) continue;
                if (posB.distance(new Vector2D(x, y)) > rB) continue;
                // Map to texA's texture space
                int ax = worldToTexture(x, posA.x, rA, texA.getTexWidth());
                int ay = worldToTexture(y, posA.y, rA, texA.getTexHeight());
                // Map to texB's texture space
                int bx = worldToTexture(x, posB.x, rB, texB.getTexWidth());
                int by = worldToTexture(y, posB.y, rB, texB.getTexHeight());
                // Check alpha
                if (inBounds(ax, ay, texA) && inBounds(bx, by, texB)
                    && texA.getAlphaMask()[ax][ay] && texB.getAlphaMask()[bx][by]) {
                    return true;
                }
            }
        }
        return false;
    }

    // Per-pixel collision for one textured body and one circle
    private boolean perPixelCircleCollision(Body texBody, Body circBody) {
        Vector2D posA = texBody.getPosition();
        Vector2D posB = circBody.getPosition();
        double rA = texBody.getSize();
        double rB = circBody.getSize();

        // Find overlap bounding box in world coordinates
        double minX = Math.max(posA.x - rA, posB.x - rB);
        double maxX = Math.min(posA.x + rA, posB.x + rB);
        double minY = Math.max(posA.y - rA, posB.y - rB);
        double maxY = Math.min(posA.y + rA, posB.y + rB);

        int steps = Math.max(texBody.getTexWidth(), 16);
        double stepX = (maxX - minX) / steps;
        double stepY = (maxY - minY) / steps;

        for (double x = minX; x <= maxX; x += stepX) {
            for (double y = minY; y <= maxY; y += stepY) {
                if (posA.distance(new Vector2D(x, y)) > rA) continue;
                if (posB.distance(new Vector2D(x, y)) > rB) continue;
                int ax = worldToTexture(x, posA.x, rA, texBody.getTexWidth());
                int ay = worldToTexture(y, posA.y, rA, texBody.getTexHeight());
                if (inBounds(ax, ay, texBody) && texBody.getAlphaMask()[ax][ay]) {
                    return true;
                }
            }
        }
        return false;
    }

    private int worldToTexture(double coord, double center, double radius, int texSize) {
        // Map world coordinate to [0, texSize-1]
        double norm = (coord - (center - radius)) / (2 * radius);
        return (int)(norm * (texSize - 1));
    }

    private boolean inBounds(int x, int y, Body body) {
        return x >= 0 && x < body.getTexWidth() && y >= 0 && y < body.getTexHeight();
    }

    private void resolveCollision(Body bodyA, Body bodyB) {
        Vector2D delta = bodyB.getPosition().subtract(bodyA.getPosition());
        Double distance = delta.magnitude();
        Double minDistance = bodyA.getSize() + bodyB.getSize();

        if (distance == 0.0) return;

        Vector2D normal = delta.normalize();
        Double penetration = minDistance - distance;

        if (penetration > 0) {
            Double totalMass = bodyA.getMass() + bodyB.getMass();
            Vector2D correction = normal.scale(penetration / totalMass);

            synchronized (bodyA) {
                bodyA.move(bodyA.getPosition().subtract(correction.scale(bodyB.getMass())));
            }
            synchronized (bodyB) {
                bodyB.move(bodyB.getPosition().add(correction.scale(bodyA.getMass())));
            }
        }

        Vector2D relativeVelocity = bodyB.getVelocity().subtract(bodyA.getVelocity());
        double velAlongNormal = relativeVelocity.dot(normal);

        if (velAlongNormal >= 0) return;

        double restitution = 0.8;

        double impulseScalar = -(1 + restitution) * velAlongNormal / 
                (1 / bodyA.getMass() + 1 / bodyB.getMass());

        Vector2D impulse = normal.scale(impulseScalar);

        synchronized (bodyA) {
            bodyA.setVelocity(bodyA.getVelocity().subtract(impulse.scale(1 / bodyA.getMass())));
        }
        synchronized (bodyB) {
            bodyB.setVelocity(bodyB.getVelocity().add(impulse.scale(1 / bodyB.getMass())));
        }

        bodyA.setColliding(true);
        bodyB.setColliding(true);
    }


    public void moveBodies() {
        ArrayList<Callable<Void>> tasks = new ArrayList<>();

        for (Body body : bodies) {
            tasks.add(() -> {
                body.move(body.getPosition().add(body.getVelocity().scale(adjustedDeltaT)));
                return null;
            });
        }

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void applyGravity(boolean isNBody, double gravity) {
    if (!isNBody) {
        ArrayList<Callable<Void>> tasks = new ArrayList<>();
        for (Body body : bodies) {
            tasks.add(() -> {
                directionalGravity(body, gravity);
                return null;
            });
        }

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    } else {
        ArrayList<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < bodies.size(); i++) {
            Body bodyA = bodies.get(i);
            for (int j = i + 1; j < bodies.size(); j++) {
                Body bodyB = bodies.get(j);

                tasks.add(() -> {
                    bodyGravity(bodyA, bodyB, gravity);
                    return null;
                });
            }
        }

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

    public void bodyGravity(Body bodyA, Body bodyB, Double gravity){
        Vector2D direction = bodyB.getPosition().subtract(bodyA.getPosition());
        Double distance = direction.magnitude();
        Vector2D normal = direction.normalize();

        Double forceMagnitude = (gravity * bodyA.getMass() * bodyB.getMass()) / (distance * distance);
        Vector2D accelerationA = normal.scale(forceMagnitude / bodyA.getMass());
        Vector2D accelerationB = normal.scale(-forceMagnitude / bodyB.getMass());

        // Update velocities
        synchronized (bodyA) {
            bodyA.setVelocity(bodyA.getVelocity().add(accelerationA.scale(adjustedDeltaT)));
        }
        synchronized (bodyB) {
            bodyB.setVelocity(bodyB.getVelocity().add(accelerationB.scale(adjustedDeltaT)));
        }
    }

    public void directionalGravity(Body body, Double gravity){
        Vector2D gravityVector = new Vector2D(0.0, gravity);
        body.setVelocity(body.getVelocity().add(gravityVector.scale(adjustedDeltaT)));
    }

    private void waitForTasks() {
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void shutdownExecutor() {
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }

    private static class CollisionPair {
        Body bodyA, bodyB;

        CollisionPair(Body a, Body b) {
            this.bodyA = a;
            this.bodyB = b;
        }
    }
}
