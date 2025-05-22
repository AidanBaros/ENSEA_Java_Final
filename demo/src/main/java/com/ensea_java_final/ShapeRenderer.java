package com.ensea_java_final;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;

/**
 * Provides utility methods for rendering basic shapes.
 */
public class ShapeRenderer {

    /**
     * Draws a solid circle at the given center coordinates.
     * @param cx     Center X coordinate
     * @param cy     Center Y coordinate
     * @param radius Radius of the circle
     * @param segments Number of segments to approximate the circle
     */
    public static void drawCircle(float cx, float cy, float radius, int segments) {
        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(cx, cy);
        double delta = 2 * Math.PI / segments;
        for (int i = 0; i <= segments; i++) {
            double angle = i * delta;
            glVertex2f(
                cx + (float)Math.cos(angle) * radius,
                cy + (float)Math.sin(angle) * radius
            );
        }
        glEnd();
    }

    /**
     * Draws a textured circle at the given center using the bound texture.
     * @param cx        Center X coordinate
     * @param cy        Center Y coordinate
     * @param radius    Radius of the circle
     * @param segments  Number of segments to approximate the circle
     * @param textureId OpenGL texture ID to bind before drawing
     */
    public static void drawTexturedCircle(float cx, float cy, float radius, int segments, int textureId) {
        glEnable(GL_MULTISAMPLE);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glColor3f(1f, 1f, 1f);

        glBegin(GL_TRIANGLE_FAN);
        glTexCoord2f(0.5f, 0.5f);
        glVertex2f(cx, cy);
        double delta = 2 * Math.PI / segments;
        for (int i = 0; i <= segments; i++) {
            double angle = i * delta;
            float x = cx + (float)Math.cos(angle) * radius;
            float y = cy + (float)Math.sin(angle) * radius;
            float u = 0.5f + 0.5f * (float)Math.cos(angle);
            float v = 0.5f + 0.5f * (float)Math.sin(angle);
            glTexCoord2f(u, v);
            glVertex2f(x, y);
        }
        glEnd();

        glBindTexture(GL_TEXTURE_2D, 0);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_MULTISAMPLE);
    }
}
