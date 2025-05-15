package com.ensea_java_final;

import java.util.ArrayList;

public class PhysicsEngine {
    private static ArrayList<Body> bodies = new ArrayList<Body>();
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
}
