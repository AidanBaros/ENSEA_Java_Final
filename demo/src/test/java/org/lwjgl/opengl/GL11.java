package org.lwjgl.opengl;

import java.nio.ByteBuffer;

public final class GL11 {

    // constants
    public static final int GL_TRIANGLE_FAN = 0x0006;
    public static final int GL_COLOR_BUFFER_BIT = 0x4000;
    public static final int GL_PROJECTION = 0x1701;
    public static final int GL_MODELVIEW = 0x1700;


    public static final int GL_TEXTURE_2D = 0x0DE1;
    public static final int GL_RGBA = 0x1908;
    public static final int GL_UNSIGNED_BYTE = 0x1401;
    public static final int GL_LINEAR = 0x2601;

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
    public static void glEnable(int cap) {

    }
    public static void glDisable(int cap) {

    }
    public static int glGenTextures() { 
        return 1;
    }
    
    public static void glDeleteTextures(int id) {

    }
    public static void glBindTexture(int target, int tex) {

    }
    public static void glTexImage2D(int target, int level, int internalformat, int width, 
    int height, int border,
    int format, int type, ByteBuffer data) {

    }
    public static void glTexParameteri(int target, int pname, int param) {

    } 
    public static void glTexCoord2f(float s, float t) {

    }
    public static void glColor3f(float r, float g, float b) {

    }


}