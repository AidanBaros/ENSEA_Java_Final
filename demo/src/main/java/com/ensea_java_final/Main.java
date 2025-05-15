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
    private int tickRate = 50;
    private int tick = 0;

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        WindowManager.init(800, 600, "Bouncing Ball");
        window = WindowManager.getWindow();

        createCapabilities();

        tick++;
        if (tick == tickRate){
            tick = 0;
            loop();
        }

        WindowManager.cleanup();
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT);

            BallRenderer.drawCircle(x, y, radius, 32);

            // Simple bouncing logic
            if (x + radius >= WindowManager.RIGHT_BOUND || x - radius <= WindowManager.LEFT_BOUND) dx = -dx;
            if (y + radius >= WindowManager.TOP_BOUND || y - radius <= WindowManager.BOTTOM_BOUND) dy = -dy;
            x += dx;
            y += dy;

            WindowManager.update();
        }
    }
}