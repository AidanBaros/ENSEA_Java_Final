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
                double distance = bodyA.getPosition().distance(bodyB.getPosition());
                double collisionDistance = bodyA.getSize() + bodyB.getSize();

                if (distance <= collisionDistance) {
                    collisionPairs.add(new CollisionPair(bodyA, bodyB));
                }
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
