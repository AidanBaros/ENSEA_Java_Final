package com.ensea_java_final;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL.createCapabilities;

import java.util.List;

public class Main {
    private long window;
    private final List<String> scenarioFiles = ScenarioLoader.listJsonFiles("scenario");
    private Menu menu;
    private Simulation simulation;

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        initWindow(800, 600, "Physics Simulation");
        TextRenderer.init("fonts/Roboto-Regular.ttf");

        menu = new Menu(window, scenarioFiles);
        simulation = new Simulation(window);

        gameLoop();
        cleanup();
    }

    private void initWindow(int width, int height, String title) {
        glfwInit();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_ANY_PROFILE);

        WindowManager.init(width, height, title);
        window = WindowManager.getWindow();
        createCapabilities();
    }

    private void gameLoop() {
        while (!glfwWindowShouldClose(window)) {
            pollAndClear();

            if (menu.isActive()) {
                menu.update();
                TextRenderer.begin();
                menu.render();
                TextRenderer.end();

                if (menu.shouldExit()) {
                    glfwSetWindowShouldClose(window, true);
                    break;
                }

                if (menu.shouldStartSimulation()) {
                    simulation.loadScenario("scenario/" + menu.getSelectedFile());
                    menu.deactivate();
                }
            } else {
                simulation.update();
                simulation.render();

                if (simulation.isBackToMenuRequested()) {
                    menu.activate();
                }
            }

            WindowManager.update();
        }
    }

    private void pollAndClear() {
        glfwPollEvents();
        glEnable(GL_TEXTURE_2D);
        glClear(GL_COLOR_BUFFER_BIT);
    }

    private void cleanup() {
        simulation.shutdown();
        WindowManager.cleanup();
    }
}
