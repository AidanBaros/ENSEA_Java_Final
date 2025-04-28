package com.ensea_java_final;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL.*;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;


public class Main {
    private long window;
    private float x = 0.0f, y = 0.0f; //ball pos
    private float dx = 0.01f, dy = 0.015f; //ball vel
    private final float radius = 0.1f;

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        WindowManager.init(800, 600, "Bouncing Ball");
        window = WindowManager.getWindow();

        createCapabilities();

        loop();

        WindowManager.cleanup();
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT);

            BallRenderer.drawCircle(x, y, radius, 32);

            // Simple bouncing logic
            if (x + radius >= 1.0f || x - radius <= -1.0f) dx = -dx;
            if (y + radius >= 1.0f || y - radius <= -1.0f) dy = -dy;
            x += dx;
            y += dy;

            WindowManager.update();
        }
    }
}