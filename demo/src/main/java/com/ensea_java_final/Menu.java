package com.ensea_java_final;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.nio.IntBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;

public class Menu {
    private final long window;
    private final List<String> scenarioFiles;

    private boolean active = true;
    private int selectedIndex = 0;
    private boolean exitRequested = false;
    private boolean startRequested = false;

    // Time-based input debouncing
    private double lastInputTime = 0.0;
    private static final double DEBOUNCE_INTERVAL = 0.15; // seconds

    public Menu(long window, List<String> scenarioFiles) {
        this.window = window;
        this.scenarioFiles = scenarioFiles;
    }

    public void update() {
        double now = glfwGetTime();
        if (now - lastInputTime < DEBOUNCE_INTERVAL) return;

        if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
            selectedIndex = (selectedIndex + 1) % (scenarioFiles.size() + 1);
            lastInputTime = now;
        }
        if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
            selectedIndex = (selectedIndex - 1 + scenarioFiles.size() + 1) % (scenarioFiles.size() + 1);
            lastInputTime = now;
        }
        if (glfwGetKey(window, GLFW_KEY_ENTER) == GLFW_PRESS) {
            if (selectedIndex == scenarioFiles.size()) {
                exitRequested = true;
            } else {
                startRequested = true;
            }
            lastInputTime = now;
        }
    }

    public void render() {
        // fetch framebuffer dimensions
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        glfwGetFramebufferSize(window, w, h);
        float fw = w.get(0);

        TextRenderer.begin();
        for (int i = 0; i < scenarioFiles.size(); i++) {
            boolean sel = (i == selectedIndex);
            float r = sel ? 1f : 1f;
            float g = sel ? 0f : 1f;
            float b = sel ? 0f : 1f;
            glColor3f(r, g, b);
            TextRenderer.drawCentered(fw / 2f, 100 + i * 30, "Start: " + scenarioFiles.get(i));
        }
        // Quit option
        boolean quitSel = (selectedIndex == scenarioFiles.size());
        glColor3f(quitSel ? 1f : 1f, quitSel ? 0f : 1f, quitSel ? 0f : 1f);
        TextRenderer.drawCentered(
            fw / 2f,
            100 + scenarioFiles.size() * 30,
            "Quit"
        );
        TextRenderer.end();
    }

    public boolean isActive() {
        return active;
    }

    public boolean shouldExit() {
        return exitRequested;
    }

    public boolean shouldStartSimulation() {
        return startRequested;
    }

    public String getSelectedFile() {
        return scenarioFiles.get(selectedIndex);
    }

    public void activate() {
        active = true;
        exitRequested = false;
        startRequested = false;
        lastInputTime = glfwGetTime();
    }

    public void deactivate() {
        active = false;
        exitRequested = false;
        startRequested = false;
    }
}
