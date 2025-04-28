package com.ensea_java_final;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL.*;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;

public class WindowManager {
    private static long window;

    public static void init(int width, int height, String title) {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(width, height, title, 0, 0);
        if (window == 0)
            throw new RuntimeException("Failed to create GLFW window");

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window,
                (vidmode.width() - width) / 2,
                (vidmode.height() - height) / 2);

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
    }

    public static long getWindow() {
        return window;
    }

    public static void update() {
        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    public static void cleanup() {
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
