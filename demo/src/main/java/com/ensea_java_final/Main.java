package com.ensea_java_final;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import java.util.List;
import java.util.ArrayList;
import static org.lwjgl.opengl.GL.*;

public class Main {
    private long window;

    private ArrayList<Body> bodies = new ArrayList<Body>();
    private PhysicsEngine physicsEngine = new PhysicsEngine(bodies);

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        WindowManager.init(800, 600, "Physics Simulation");
        window = WindowManager.getWindow();

        createCapabilities();

        // Add bodies here
        bodies.add(new Body.Builder().mass(1.0).size(0.1).position(0.0, 0.0).velocity(1.0, 0.0).build()); //add call to fixed()
        bodies.add(new Body.Builder().mass(1.0).size(0.1).position(0.5, 0.0).velocity(0.0, 1.0).build());
        bodies.add(new Body.Builder().mass(1.0).size(0.1).position(0.0, 0.5).velocity(0.0, -1.0).build());
        bodies.add(new Body.Builder().mass(1.0).size(0.1).position(0.5, 0.5).velocity(-1.0, 0.0).build());

        loop();


        WindowManager.cleanup();
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT);

            physicsEngine.update();
            for (Body body:bodies) {
                body.draw();
            }

            WindowManager.update();
        }
    }
}
