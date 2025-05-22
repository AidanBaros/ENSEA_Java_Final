package com.ensea_java_final;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

public class WindowManager {
    public static float LEFT_BOUND;
    public static float RIGHT_BOUND;
    public static float BOTTOM_BOUND;
    public static float TOP_BOUND;

    private static long window;

    public static void init(int width, int height, String title) {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 4);

        long primary = glfwGetPrimaryMonitor();
        GLFWVidMode mode = glfwGetVideoMode(primary);
        int screenW = mode.width();
        int screenH = mode.height();

        window = glfwCreateWindow(screenW, screenH, title, 0, 0);
        if (window == 0) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        glfwSetWindowPos(window, 0, 0);
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glEnable(GL_MULTISAMPLE);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        resizeViewport(screenW, screenH);

        glfwSetFramebufferSizeCallback(window, (win, w, h) -> resizeViewport(w, h));
    }

    private static void resizeViewport(int w, int h) {
        glViewport(0, 0, w, h);
        float aspect = (float) w / h;

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        if (aspect >= 1.0f) {
            glOrtho(-aspect, aspect, -1.0, 1.0, -1.0, 1.0);
            LEFT_BOUND   = -aspect;
            RIGHT_BOUND  =  aspect;
            BOTTOM_BOUND = -1.0f;
            TOP_BOUND    =  1.0f;
        } else {
            glOrtho(-1.0, 1.0, -1.0f / aspect, 1.0f / aspect, -1.0, 1.0);
            LEFT_BOUND   = -1.0f;
            RIGHT_BOUND  =  1.0f;
            BOTTOM_BOUND = -1.0f / aspect;
            TOP_BOUND    =  1.0f / aspect;
        }

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
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