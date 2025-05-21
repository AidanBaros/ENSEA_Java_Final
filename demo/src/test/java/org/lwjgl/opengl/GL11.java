package org.lwjgl.opengl;

public final class GL11 {

    // constants
    public static final int GL_TRIANGLE_FAN = 0x0006;
    public static final int GL_COLOR_BUFFER_BIT = 0x4000;
    public static final int GL_PROJECTION = 0x1701;
    public static final int GL_MODELVIEW = 0x1700;

    // no op versions of the few methods the code under test cases

    public static void glBegin(int mode) {

    }

    public static void glVertex2f(float x, float y) {

    }

    public static void glEnd() {

    }

    public static void glViewport(int x, int y, int w, int h) {

    }

    public static void glMatrixMode(int mode) {

    }

    public static void glLoadIdentity() {

    }

    public static void glOrtho(double l, double r, double b, double t, double n, double f) {

    }

    public static void glClear(int mask) {

    }

}