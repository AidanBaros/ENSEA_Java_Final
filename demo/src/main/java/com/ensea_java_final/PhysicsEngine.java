package com.ensea_java_final;

import java.util.ArrayList;

public class PhysicsEngine {
    private static ArrayList<Body> bodies = new ArrayList<Body>();
    public static void addBody(Body b) {
        bodies.add(b);
    }
}
