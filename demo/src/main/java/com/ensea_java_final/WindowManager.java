package com.ensea_java_final;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL.*;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

public class WindowManager {
    public static float LEFT_BOUND, RIGHT_BOUND, BOTTOM_BOUND, TOP_BOUND;

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
        GL.createCapabilities();
        glfwSwapInterval(1);
        glfwShowWindow(window);

        glViewport(0, 0, width, height);
        float aspect = (float) width / height;
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        if (aspect >= 1.0f) {
            glOrtho(-aspect, aspect, -1.0, 1.0, -1.0, 1.0);
            LEFT_BOUND   = -aspect;
            RIGHT_BOUND  =  aspect;
            BOTTOM_BOUND = -1.0f;
            TOP_BOUND    =  1.0f;
        } else {
            glOrtho(-1.0, 1.0, -1.0f/aspect, 1.0f/aspect, -1.0, 1.0);
            LEFT_BOUND   = -1.0f;
            RIGHT_BOUND  =  1.0f;
            BOTTOM_BOUND = -1.0f/aspect;
            TOP_BOUND    =  1.0f/aspect;        
        }
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glfwSetFramebufferSizeCallback(window, (win, w, h) -> {
            glViewport(0, 0, w, h);
            
            float ar = (float) w / h;  
            glMatrixMode(GL_PROJECTION);  
            glLoadIdentity(); 
            if (ar >= 1.0f) {
                glOrtho(-ar, ar, -1.0, 1.0, -1.0, 1.0);
                LEFT_BOUND   = -ar;
                RIGHT_BOUND  =  ar;
                BOTTOM_BOUND = -1.0f;
                TOP_BOUND    =  1.0f;

            } else {
                glOrtho(-1.0, 1.0, -1.0f/ar, 1.0f/ar, -1.0, 1.0);
                LEFT_BOUND   = -1.0f;
                RIGHT_BOUND  =  1.0f;
                BOTTOM_BOUND = -1.0f/ar;
                TOP_BOUND    =  1.0f/ar;

            }
            glMatrixMode(GL_MODELVIEW); 
        });
        
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
