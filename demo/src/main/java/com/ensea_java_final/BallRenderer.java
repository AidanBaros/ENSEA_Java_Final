package com.ensea_java_final;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL.*;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;

public class BallRenderer {
    public static void drawCircle(float cx, float cy, float r, int segments) {
        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(cx, cy);
        double step = 2.0 * Math.PI / segments;
        for (int i = 0; i <= segments; i++) {
            double angle = i * step;
            glVertex2f(
                cx + (float)Math.cos(angle) * r,
                cy + (float)Math.sin(angle) * r
            );
        }
        glEnd();
    }
}
