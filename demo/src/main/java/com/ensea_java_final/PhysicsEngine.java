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
        ArrayList<CollisionContact> contacts = new ArrayList<>();

        for (int i = 0; i < bodies.size(); i++) {
            Body bodyA = bodies.get(i);
            for (int j = i + 1; j < bodies.size(); j++) {
                Body bodyB = bodies.get(j);

                // Broad phase: bounding circle
                double distance = bodyA.getPosition().distance(bodyB.getPosition());
                double collisionDistance = bodyA.getSize() + bodyB.getSize();
                if (distance > collisionDistance) continue;

                // Narrow phase: per-pixel collision if either has alpha mask
                if (bodyA.hasAlphaMask() && bodyB.hasAlphaMask()) {
                    Vector2D contact = perPixelCollisionContact(bodyA, bodyB);
                    if (contact == null) continue;
                    contacts.add(new CollisionContact(bodyA, bodyB, contact));
                }
                else if (bodyA.hasAlphaMask()) {
                    Vector2D contact = perPixelCircleCollisionContact(bodyA, bodyB);
                    if (contact == null) continue;
                    contacts.add(new CollisionContact(bodyA, bodyB, contact));
                }
                else if (bodyB.hasAlphaMask()) {
                    Vector2D contact = perPixelCircleCollisionContact(bodyB, bodyA);
                    if (contact == null) continue;
                    contacts.add(new CollisionContact(bodyA, bodyB, contact));
                }
                else {
                    // fallback: bounding circle
                    collisionPairs.add(new CollisionPair(bodyA, bodyB));
                }
            }
        }

        ArrayList<Callable<Void>> tasks = new ArrayList<>();
        for (CollisionPair pair : collisionPairs) {
            tasks.add(() -> {
                resolveCollision(pair.bodyA, pair.bodyB, null);
                return null;
            });
        }
        for (CollisionContact contact : contacts) {
            tasks.add(() -> {
                resolveCollision(contact.bodyA, contact.bodyB, contact.contactPoint);
                return null;
            });
        }

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Returns the first contact point (world coordinates) for two textured bodies, or null if none
    private Vector2D perPixelCollisionContact(Body texA, Body texB) {
        Vector2D posA = texA.getPosition();
        Vector2D posB = texB.getPosition();
        double rA = texA.getSize();
        double rB = texB.getSize();

        double minX = Math.max(posA.x - rA, posB.x - rB);
        double maxX = Math.min(posA.x + rA, posB.x + rB);
        double minY = Math.max(posA.y - rA, posB.y - rB);
        double maxY = Math.min(posA.y + rA, posB.y + rB);

        int steps = Math.max(Math.max(texA.getTexWidth(), texB.getTexWidth()), 16);
        double stepX = (maxX - minX) / steps;
        double stepY = (maxY - minY) / steps;

        for (double x = minX; x <= maxX; x += stepX) {
            for (double y = minY; y <= maxY; y += stepY) {
                if (posA.distance(new Vector2D(x, y)) > rA) continue;
                if (posB.distance(new Vector2D(x, y)) > rB) continue;
                int ax = worldToTexture(x, posA.x, rA, texA.getTexWidth());
                int ay = worldToTexture(y, posA.y, rA, texA.getTexHeight());
                int bx = worldToTexture(x, posB.x, rB, texB.getTexWidth());
                int by = worldToTexture(y, posB.y, rB, texB.getTexHeight());
                if (inBounds(ax, ay, texA) && inBounds(bx, by, texB)
                    && texA.getAlphaMask()[ax][ay] && texB.getAlphaMask()[bx][by]) {
                    return new Vector2D(x, y);
                }
            }
        }
        return null;
    }

    // Returns the first contact point (world coordinates) for a textured and a circle body, or null if none
    private Vector2D perPixelCircleCollisionContact(Body texBody, Body circBody) {
        Vector2D posA = texBody.getPosition();
        Vector2D posB = circBody.getPosition();
        double rA = texBody.getSize();
        double rB = circBody.getSize();

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
                    return new Vector2D(x, y);
                }
            }
        }
        return null;
    }

    private int worldToTexture(double coord, double center, double radius, int texSize) {
        double norm = (coord - (center - radius)) / (2 * radius);
        return (int)(norm * (texSize - 1));
    }

    private boolean inBounds(int x, int y, Body body) {
        return x >= 0 && x < body.getTexWidth() && y >= 0 && y < body.getTexHeight();
    }

    // Modified to use contactPoint if provided (for per-pixel collision)
    private void resolveCollision(Body bodyA, Body bodyB, Vector2D contactPoint) {
        Vector2D posA = bodyA.getPosition();
        Vector2D posB = bodyB.getPosition();

        Vector2D normal;
        double penetration;

        if (contactPoint != null) {
            boolean aTextured = bodyA.hasAlphaMask();
            boolean bTextured = bodyB.hasAlphaMask();
            if (aTextured && !bTextured) {
                normal = contactPoint.subtract(posB).normalize();
                penetration = bodyB.getSize() - posB.distance(contactPoint);
            } else if (!aTextured && bTextured) {
                normal = posA.subtract(contactPoint).normalize();
                penetration = bodyA.getSize() - posA.distance(contactPoint);
            } else {
                normal = posA.subtract(posB).normalize();
                penetration = Math.min(
                    bodyA.getSize() - posA.distance(contactPoint),
                    bodyB.getSize() - posB.distance(contactPoint)
                );
            }
        } else {
            Vector2D delta = posB.subtract(posA);
            double distance = delta.magnitude();
            double minDistance = bodyA.getSize() + bodyB.getSize();
            if (distance == 0.0) return;
            normal = delta.normalize();
            penetration = minDistance - distance;
        }

        // --- Improved stabilization and velocity clamping ---
        double slop = 1e-2; // Allowable penetration before correction
        double percent = 0.05; // Correction percent (5%)
        double maxVelocity = 2.0; // Maximum allowed velocity after collision

        if (penetration > slop) {
            Double totalMass = bodyA.getMass() + bodyB.getMass();
            Vector2D correction = normal.scale(percent * (penetration - slop) / totalMass);
            synchronized (bodyA) {
                bodyA.move(bodyA.getPosition().subtract(correction.scale(bodyB.getMass())));
            }
            synchronized (bodyB) {
                bodyB.move(bodyB.getPosition().add(correction.scale(bodyA.getMass())));
            }
        }

        Vector2D relativeVelocity = bodyB.getVelocity().subtract(bodyA.getVelocity());
        double velAlongNormal = relativeVelocity.dot(normal);

        // Only apply impulse if bodies are moving toward each other and penetration is significant
        if (velAlongNormal >= 0 || penetration <= slop) return;

        double restitution = 0.2; // Lower restitution for stability

        double impulseScalar = -(1 + restitution) * velAlongNormal /
                (1 / bodyA.getMass() + 1 / bodyB.getMass());

        Vector2D impulse = normal.scale(impulseScalar);

        synchronized (bodyA) {
            Vector2D newVel = bodyA.getVelocity().subtract(impulse.scale(1 / bodyA.getMass()));
            bodyA.setVelocity(clampVelocity(newVel, maxVelocity));
        }
        synchronized (bodyB) {
            Vector2D newVel = bodyB.getVelocity().add(impulse.scale(1 / bodyB.getMass()));
            bodyB.setVelocity(clampVelocity(newVel, maxVelocity));
        }

        bodyA.setColliding(true);
        bodyB.setColliding(true);
    }

    // Clamp velocity vector to a maximum magnitude
    private Vector2D clampVelocity(Vector2D v, double maxVel) {
        double mag = v.magnitude();
        if (mag > maxVel) {
            return v.normalize().scale(maxVel);
        }
        return v;
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

    // Helper class for per-pixel collision contacts
    private static class CollisionContact {
        Body bodyA, bodyB;
        Vector2D contactPoint;
        CollisionContact(Body a, Body b, Vector2D contact) {
            this.bodyA = a;
            this.bodyB = b;
            this.contactPoint = contact;
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
