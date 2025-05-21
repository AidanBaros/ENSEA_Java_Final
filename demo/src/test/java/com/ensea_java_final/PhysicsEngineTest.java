package com.ensea_java_final;

import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


public class PhysicsEngineTest {

    private Body body(double x, double y) {

        return new Body.Builder()
        .mass(1.0).size(0.1).position(x,y).velocity(0.0, 0.0).build();
    }

    @Test
    void bodyGravityAcceleratesBothBodies() {
        Body a = body(0,0);
        Body b = body(1,0);

        PhysicsEngine engine = new PhysicsEngine(new ArrayList<>(List.of(a,b)));

        engine.bodyGravity(a, b, 1.0);

        assertAll(
            () -> assertTrue(a.getVelocity().x > 0, "a should accelerate toward b"),
            () -> assertTrue(b.getVelocity().x < 0, "b should accelerate toward a")
        );
        engine.shutdownExecutor(); // clean up thread
    }

    @Test
    void directionalGravityAddsDownwardVelocity() {
        Body p = body(0,0);

        PhysicsEngine engine = new PhysicsEngine(new ArrayList<>(List.of(p)));

        engine.directionalGravity(p, -9.8);

        assertEquals(-9.8 * 0.001, p.getVelocity().y, 1e-6);

        engine.shutdownExecutor(); // clean up

    }

    @Test
    void resolveCollisionSeparatesOverlappingBodies() {
        Body a = body(0,0);
        Body b = body(0.05, 0);

        PhysicsEngine engine = new PhysicsEngine(new ArrayList<>(List.of(a,b)));
        engine.resolveCollisions();

        assertTrue(a.getPosition().distance(b.getPosition()) >= a.getSize()+ b.getSize());

        engine.shutdownExecutor(); // clean up threads not necessary

    }

}
