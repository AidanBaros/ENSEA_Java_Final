package com.ensea_java_final;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import java.util.List;
import java.util.ArrayList;
import static org.lwjgl.opengl.GL.*;

public class Main {
    private long window;

    private List<Body> bodies;

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        WindowManager.init(800, 600, "Physics Simulation");
        window = WindowManager.getWindow();

        createCapabilities();

        bodies = new ArrayList<Body>();
        // Add bodies here
        bodies.add(new Body.Builder().mass(1.0).size(0.1).position(0.1, 0.1).velocity(0.0, 0.0).color(1.0f,1.0f,0.0f).texture("texture/face.png").build());
      
        tick++;
        if (tick == tickRate){ // use modulus?
            tick = 0;
            loop();
        }


        WindowManager.cleanup();
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT);

            for (Body body:bodies) {
                body.draw();
            }

            WindowManager.update();
        }
    }
}
