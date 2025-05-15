package com.ensea_java_final;

public class PhysicsEngine {
    private Body bodies[];
    
    public Boolean collision(Body primaryBody){
        for (Body body:bodies){
            Double distance = primaryBody.getPosition().distance(body.getPosition());
            Double thresholdDistance = (primaryBody.getSize()*2)+(body.getSize);
            if (thresholdDistance > distance){
                
            }
        }
    } 
}
