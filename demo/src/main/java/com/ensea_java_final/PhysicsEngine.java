package com.ensea_java_final;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Enumeration;


public class PhysicsEngine {
    private ArrayList<Body> bodies = new ArrayList<Body>();
    private Double simulationDeltaT = 0.001;
    private Dictionary<String, Double> gravityDict =  new Hashtable<>();
    private String gravityType = "game";

    
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

    public void update(){
        resolveCollisions();
        applyGravity(true,gravityDict.get(gravityType));
        moveBodies();
    }
    
    public void resolveCollisions() {
        int size = bodies.size();

        for (int i = 0; i < size; i++) {
            Body bodyA = bodies.get(i);
            bodyA.setColliding(false);
            for (int j = i + 1; j < size; j++) { // Avoid double-checking and self-collision
                Body bodyB = bodies.get(j);
                bodyB.setColliding(false);
                Double distance = bodyA.getPosition().distance(bodyB.getPosition());
                Double collisionDistance = bodyA.getSize() + bodyB.getSize(); // Assuming size = radius

                //System.out.println("Distance:" + distance + "   Collision Distance:" + collisionDistance);
                if (distance <= collisionDistance) {
                    bodyA.setColliding(true);
                    bodyB.setColliding(true);
                    // Compute normal
                    Vector2D normal = bodyB.getPosition().subtract(bodyA.getPosition()).normalize();

                    // Relative velocity
                    Vector2D relativeVelocity = bodyA.getVelocity().subtract(bodyB.getVelocity());

                    // Velocity along the normal
                    Double velAlongNormal = relativeVelocity.dot(normal);
                    //System.out.println(velAlongNormal);

                    // Don't resolve if already separating
                    if (velAlongNormal <= 0) { continue; }
                    //System.out.println("test");

                    // Calculate restitution (elasticity)
                    Double restitution = 1.0; // perfectly elastic

                    // Calculate impulse scalar
                    Double impulseScalar = -(1 + restitution) * velAlongNormal / 
                            (1 / bodyA.getMass() + 1 / bodyB.getMass());

                    // Apply impulse to both bodies
                    Vector2D impulse = normal.scale(impulseScalar);
                    bodyA.setVelocity(bodyA.getVelocity().add(impulse.scale(1 / bodyA.getMass())));
                    bodyB.setVelocity(bodyB.getVelocity().subtract(impulse.scale(1 / bodyB.getMass())));
                }
                //System.out.println("    Body A:" + bodyA.isColliding() + "   Body B:" + bodyB.isColliding());
            }
        }
    } 

    public void moveBodies() {
        for (Body body:bodies) {
            body.move(body.getPosition().add(body.getVelocity().scale(simulationDeltaT)));
        }
    }

    public void applyGravity(Boolean isNBody, Double gravity) {
    if (!isNBody){
        for (Body body : bodies) {
            directionalGravity(body, gravity);
        }
    }
    else{
        for (int i = 0; i < bodies.size(); i++) {
            Body bodyA = bodies.get(i);
            for (int j = i + 1; j < bodies.size(); j++) {
                Body bodyB = bodies.get(j);
                bodyGravity(bodyA, bodyB, gravity);
            }
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
        bodyA.setVelocity(bodyA.getVelocity().add(accelerationA.scale(simulationDeltaT)));
        bodyB.setVelocity(bodyB.getVelocity().add(accelerationB.scale(simulationDeltaT)));
    }

    public void directionalGravity(Body body, Double gravity){
        Vector2D gravityVector = new Vector2D(0.0, gravity);
        body.setVelocity(body.getVelocity().add(gravityVector.scale(simulationDeltaT)));
    }
}
