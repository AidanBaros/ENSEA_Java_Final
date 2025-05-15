package com.ensea_java_final;

import java.util.ArrayList;

public class PhysicsEngine {
    private static ArrayList<Body> bodies = new ArrayList<Body>();
    private static double simulationDeltaT = 0.001;
    public static void addBody(Body b) {
        bodies.add(b);
    }
    
    public Boolean collision(Body primaryBody){
        for (Body body:bodies){
            Double distance = primaryBody.getPosition().distance(body.getPosition());
            Double thresholdDistance = (primaryBody.getSize()*2)+(body.getSize);
            if (thresholdDistance > distance){
                
            }
        }
    } 

    public void moveBodies() {
        for (Body body:bodies) {
            body.move(body.getPosition().add(body.getVelocity().scale(simulationDeltaT)))
        }
    }
}
