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
    // Configuration fields
    private Double simulationDeltaT = 0.001;
    private Double timeScale = 1.0;
    private Double adjustedDeltaT = simulationDeltaT * timeScale;

    // State
    private ArrayList<Body> bodies = new ArrayList<>();
    private Dictionary<String, Double> gravityDict = new Hashtable<>();
    private String gravityType = "game";

    // Concurrency
    private final ExecutorService executor =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public PhysicsEngine(ArrayList<Body> bodies) {
        this.bodies = bodies;
        gravityDict.put("standard", 6.67430e-11);
        gravityDict.put("earth", -9.8);
        gravityDict.put("game", 1.0);
    }

    // Public API
    public Double getTimeScale() {
        return timeScale;
    }

    public void setTimeScale(Double timeScale) {
        this.timeScale = timeScale;
    }

    public void setGravityType(String gravityType) {
        if (gravityDict.get(gravityType) != null) {
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

    public void shutdownExecutor() {
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }

    // Core simulation steps
    public void applyGravity(boolean isNBody, double gravity) {
        if (!isNBody) {
            runTasks(b -> directionalGravity(b, gravity));
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
            invokeAll(tasks);
        }
    }

    public void moveBodies() {
        runTasks(b -> b.move(b.getPosition().add(b.getVelocity().scale(adjustedDeltaT))));
    }

    public void resolveCollisions() {
        ArrayList<CollisionPair> pairs = new ArrayList<>();
        for (int i = 0; i < bodies.size(); i++) {
            Body a = bodies.get(i);
            for (int j = i + 1; j < bodies.size(); j++) {
                Body b = bodies.get(j);
                double dist = a.getPosition().distance(b.getPosition());
                if (dist <= a.getSize() + b.getSize()) {
                    pairs.add(new CollisionPair(a, b));
                }
            }
        }

        ArrayList<Callable<Void>> tasks = new ArrayList<>();
        for (CollisionPair p : pairs) {
            tasks.add(() -> {
                resolveCollision(p.bodyA, p.bodyB);
                return null;
            });
        }
        invokeAll(tasks);
    }

    // Gravity implementations
    public void bodyGravity(Body a, Body b, Double gravity) {
        Vector2D dir = b.getPosition().subtract(a.getPosition());
        Double dist = dir.magnitude();
        Vector2D normal = dir.normalize();
        Double force = (gravity * a.getMass() * b.getMass()) / (dist * dist);

        Vector2D accA = normal.scale(force / a.getMass());
        Vector2D accB = normal.scale(-force / b.getMass());

        synchronized (a) {
            a.setVelocity(a.getVelocity().add(accA.scale(adjustedDeltaT)));
        }
        synchronized (b) {
            b.setVelocity(b.getVelocity().add(accB.scale(adjustedDeltaT)));
        }
    }

    public void directionalGravity(Body body, Double gravity) {
        Vector2D grav = new Vector2D(0.0, gravity);
        body.setVelocity(body.getVelocity().add(grav.scale(adjustedDeltaT)));
    }

    // Collision resolution
    private void resolveCollision(Body a, Body b) {
        Vector2D delta = b.getPosition().subtract(a.getPosition());
        Double dist = delta.magnitude();
        Double minDist = a.getSize() + b.getSize();
        if (dist == 0.0) return;

        Vector2D normal = delta.normalize();
        Double penetration = minDist - dist;
        if (penetration > 0) {
            Double totalMass = a.getMass() + b.getMass();
            Vector2D correction = normal.scale(penetration / totalMass);
            synchronized (a) {
                a.move(a.getPosition().subtract(correction.scale(b.getMass())));
            }
            synchronized (b) {
                b.move(b.getPosition().add(correction.scale(a.getMass())));
            }
        }

        Vector2D relVel = b.getVelocity().subtract(a.getVelocity());
        double velAlongNormal = relVel.dot(normal);
        if (velAlongNormal >= 0) return;

        double restitution = 0.8;
        double impulseScalar = -(1 + restitution) * velAlongNormal
            / (1 / a.getMass() + 1 / b.getMass());
        Vector2D impulse = normal.scale(impulseScalar);

        synchronized (a) {
            a.setVelocity(a.getVelocity().subtract(impulse.scale(1 / a.getMass())));
        }
        synchronized (b) {
            b.setVelocity(b.getVelocity().add(impulse.scale(1 / b.getMass())));
        }

        a.setColliding(true);
        b.setColliding(true);
    }

    // Utility for parallel tasks
    private void runTasks(java.util.function.Consumer<Body> action) {
        ArrayList<Callable<Void>> tasks = new ArrayList<>();
        for (Body b : bodies) {
            tasks.add(() -> {
                action.accept(b);
                return null;
            });
        }
        invokeAll(tasks);
    }

    private void invokeAll(ArrayList<Callable<Void>> tasks) {
        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Inner types
    private static class CollisionPair {
        Body bodyA, bodyB;

        CollisionPair(Body a, Body b) {
            this.bodyA = a;
            this.bodyB = b;
        }
    }
}