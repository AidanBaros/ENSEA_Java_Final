package com.ensea_java_final;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PhysicsEngine {
    private final List<Body> bodies;
    private static final double BASE_DELTA = 0.001;
    private double timeScale = 1.0;

    private final Map<String,Double> gravityMap = Map.of(
        "standard", 6.67430e-11,
        "earth",    -9.8,
        "game",      1.0
    );
    private String gravityType = "game";

    private final ExecutorService executor =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public PhysicsEngine(List<Body> bodies) {
        this.bodies = bodies;
    }

    public double getTimeScale()       { return timeScale; }
    public void setTimeScale(double t) { timeScale = t; }

    public void setGravityType(String t) {
        if (gravityMap.containsKey(t)) gravityType = t;
        else {
            System.err.println("Invalid gravity: " + t + ", defaulting to game");
            gravityType = "game";
        }
    }

    public void update() {
        double delta = BASE_DELTA * timeScale;
        applyGravity(delta);
        moveBodies(delta);
        resolveCollisions();
    }

    private void applyGravity(double delta) {
        double g = gravityMap.getOrDefault(gravityType, 1.0);
        if ("standard".equals(gravityType)) {
            applyNBodyGravity(g, delta);
        } else {
            applyDirectionalGravity(g, delta);
        }
    }

    private void applyDirectionalGravity(double gravity, double delta) {
        List<Callable<Void>> tasks = new ArrayList<>();
        for (Body b : bodies) {
            tasks.add(() -> {
                synchronized (b) {
                    b.setVelocity(b.getVelocity().add(new Vector2D(0, gravity).scale(delta)));
                }
                return null;
            });
        }
        invokeTasks(tasks);
    }

    private void applyNBodyGravity(double G, double delta) {
        List<Callable<Void>> tasks = new ArrayList<>();
        int n = bodies.size();
        for (int i = 0; i < n; i++) {
            Body a = bodies.get(i);
            for (int j = i + 1; j < n; j++) {
                Body b = bodies.get(j);
                tasks.add(() -> {
                    Vector2D diff = b.getPosition().subtract(a.getPosition());
                    double dist2 = diff.magnitudeSquared() + 1e-6;
                    Vector2D normal = diff.normalize();
                    double force = G * a.getMass() * b.getMass() / dist2;
                    Vector2D accA = normal.scale(force / a.getMass());
                    Vector2D accB = normal.scale(-force / b.getMass());
                    synchronized (a) { a.setVelocity(a.getVelocity().add(accA.scale(delta))); }
                    synchronized (b) { b.setVelocity(b.getVelocity().add(accB.scale(delta))); }
                    return null;
                });
            }
        }
        invokeTasks(tasks);
    }

    private void moveBodies(double delta) {
        List<Callable<Void>> tasks = new ArrayList<>();
        for (Body b : bodies) {
            tasks.add(() -> {
                if (!b.isFixed()) {
                    Vector2D np = b.getPosition().add(b.getVelocity().scale(delta));
                    synchronized (b) { b.setPosition(np); }
                }
                return null;
            });
        }
        invokeTasks(tasks);
    }

    private void resolveCollisions() {
        // ... your existing collision logic ...
    }

    private void invokeTasks(List<Callable<Void>> tasks) {
        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void shutdownExecutor() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}