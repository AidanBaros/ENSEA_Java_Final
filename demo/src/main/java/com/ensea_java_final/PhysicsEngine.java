package com.ensea_java_final;

import java.util.ArrayList;

public class PhysicsEngine {
    private static ArrayList<Body> bodies = new ArrayList<Body>();
    public static void addBody(Body b) {
        bodies.add(b);
    }
    
    public void resolveCollisions() {
        int size = bodies.size();

        for (int i = 0; i < size; i++) {
            Body bodyA = bodies.get(i);
            for (int j = i + 1; j < size; j++) { // Avoid double-checking and self-collision
                Body bodyB = bodies.get(j);

                double distance = bodyA.getPosition().distance(bodyB.getPosition());
                double collisionDistance = bodyA.getSize() + bodyB.getSize(); // Assuming size = radius

                if (distance < collisionDistance) {
                    // Compute normal
                    Vector2D normal = bodyB.getPosition().subtract(bodyA.getPosition()).normalize();

                    // Relative velocity
                    Vector2D relativeVelocity = bodyA.getVelocity().subtract(bodyB.getVelocity());

                    // Velocity along the normal
                    double velAlongNormal = relativeVelocity.dot(normal);

                    // Don't resolve if already separating
                    if (velAlongNormal > 0) continue;

                    // Calculate restitution (elasticity)
                    double restitution = 1.0; // perfectly elastic

                    // Calculate impulse scalar
                    double impulseScalar = -(1 + restitution) * velAlongNormal / 
                            (1 / bodyA.getMass() + 1 / bodyB.getMass());

                    // Apply impulse to both bodies
                    Vector2D impulse = normal.scale(impulseScalar);
                    bodyA.setVelocity(bodyA.getVelocity().add(impulse.scale(1 / bodyA.getMass())));
                    bodyB.setVelocity(bodyB.getVelocity().subtract(impulse.scale(1 / bodyB.getMass())));
                }
            }
        }
    }
}
