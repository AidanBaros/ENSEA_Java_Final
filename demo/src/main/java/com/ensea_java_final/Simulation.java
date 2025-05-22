package com.ensea_java_final;

import static org.lwjgl.glfw.GLFW.*;

import java.util.List;  // ← added

public class Simulation {
    private final long window;
    private List<Body> bodies;
    private PhysicsEngine physicsEngine;

    private boolean backToMenuRequested = false;

    // Time‐based input debouncing
    private double lastInputTime;
    private static final double DEBOUNCE_INTERVAL = 0.15; // seconds

    public Simulation(long window) {
        this.window = window;
        this.lastInputTime = glfwGetTime();
    }

    public void loadScenario(String path) {
        bodies = ScenarioLoader.load(path);
        physicsEngine = new PhysicsEngine(bodies);
        physicsEngine.setGravityType("game");
        glfwSetWindowTitle(window, ScenarioLoader.getSimulationName(path));
        System.out.println("Loaded scenario: " + path);
    }

    public void update() {
        handleInput();
        physicsEngine.update();
    }

    public void render() {
        for (Body b : bodies) {
            b.draw();
        }
    }

    private void handleInput() {
        double now = glfwGetTime();
        if (now - lastInputTime < DEBOUNCE_INTERVAL) return;

        boolean handled = false;

        if (glfwGetKey(window, GLFW_KEY_EQUAL) == GLFW_PRESS
         || glfwGetKey(window, GLFW_KEY_KP_ADD) == GLFW_PRESS) {
            double current = physicsEngine.getTimeScale();
            physicsEngine.setTimeScale(Math.min(current + 0.1, 5.0));
            handled = true;
        } else if (glfwGetKey(window, GLFW_KEY_MINUS) == GLFW_PRESS
                || glfwGetKey(window, GLFW_KEY_KP_SUBTRACT) == GLFW_PRESS) {
            double current = physicsEngine.getTimeScale();
            physicsEngine.setTimeScale(Math.max(current - 0.1, 1.0));
            handled = true;
        } else if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            backToMenuRequested = true;
            handled = true;
        }

        if (handled) {
            System.out.println("Time scale: " + physicsEngine.getTimeScale());
            lastInputTime = now;
        }
    }

    public boolean isBackToMenuRequested() {
        return backToMenuRequested;
    }

    public void shutdown() {
        physicsEngine.shutdownExecutor();
    }
}