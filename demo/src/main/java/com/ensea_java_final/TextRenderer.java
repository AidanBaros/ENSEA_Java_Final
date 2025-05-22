package com.ensea_java_final;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL33.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

public class TextRenderer {
    private static final int FONT_TEXTURE_SIZE = 1024;
    private static final int FIRST_CHAR = 32;
    private static final int CHAR_COUNT = 96;
    private static STBTTBakedChar.Buffer cdata;
    private static int fontTex;
    private static boolean initialized = false;

    /**
     * Initializes the font texture and glyph data from the given TTF resource path.
     */
    public static void init(String resourcePath) {
        if (initialized) return;
        try (InputStream is = TextRenderer.class.getClassLoader()
                    .getResourceAsStream(resourcePath)) {
            if (is == null) throw new IOException("Font file not found: " + resourcePath);
            byte[] bytes = is.readAllBytes();
            ByteBuffer ttf = BufferUtils.createByteBuffer(bytes.length).put(bytes);
            ttf.flip();

            ByteBuffer bitmap = BufferUtils.createByteBuffer(FONT_TEXTURE_SIZE * FONT_TEXTURE_SIZE);
            cdata = STBTTBakedChar.malloc(CHAR_COUNT);
            STBTruetype.stbtt_BakeFontBitmap(ttf, 128, bitmap,
                    FONT_TEXTURE_SIZE, FONT_TEXTURE_SIZE, FIRST_CHAR, cdata);

            fontTex = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, fontTex);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, FONT_TEXTURE_SIZE,
                    FONT_TEXTURE_SIZE, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            // swizzle red channel for RGBA output
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_RED);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_RED);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_RED);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_RED);

            initialized = true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load font for TextRenderer", e);
        }
    }

    /**
     * Prepares OpenGL state for text rendering. Call before any draw calls.
     */
    public static void begin() {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, fontTex);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
    }

    /**
     * Restores OpenGL state after text rendering.
     */
    public static void end() {
        glDisable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
    }

    /**
     * Draws the provided text centered at (centerX, y). Color set via glColor3f.
     */
    public static void drawCentered(float centerX, float y, String text) {
        // Setup orthographic projection
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        glfwGetFramebufferSize(WindowManager.getWindow(), w, h);

        glMatrixMode(GL_PROJECTION);
        glPushMatrix(); glLoadIdentity();
        glOrtho(0, w.get(0), h.get(0), 0, -1, 1);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix(); glLoadIdentity();

        float textWidth = calculateWidth(text);
        float startX = centerX - textWidth / 2f;
        float[] x = { startX }, yArr = { y };

        try (MemoryStack stack = MemoryStack.stackPush()) {
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
            for (char c : text.toCharArray()) {
                if (c < FIRST_CHAR || c >= FIRST_CHAR + CHAR_COUNT) continue;
                STBTruetype.stbtt_GetBakedQuad(cdata, FONT_TEXTURE_SIZE,
                        FONT_TEXTURE_SIZE, c - FIRST_CHAR, x, yArr, quad, true);
                glBegin(GL_QUADS);
                    glTexCoord2f(quad.s0(), quad.t0()); glVertex2f(quad.x0(), quad.y0());
                    glTexCoord2f(quad.s1(), quad.t0()); glVertex2f(quad.x1(), quad.y0());
                    glTexCoord2f(quad.s1(), quad.t1()); glVertex2f(quad.x1(), quad.y1());
                    glTexCoord2f(quad.s0(), quad.t1()); glVertex2f(quad.x0(), quad.y1());
                glEnd();
            }
        }

        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
    }

    /**
     * Calculates pixel width of the given text string.
     */
    private static float calculateWidth(String text) {
        float[] x = {0f}, y = {0f};
        try (MemoryStack stack = MemoryStack.stackPush()) {
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
            for (char c : text.toCharArray()) {
                if (c < FIRST_CHAR || c >= FIRST_CHAR + CHAR_COUNT) continue;
                STBTruetype.stbtt_GetBakedQuad(cdata, FONT_TEXTURE_SIZE,
                        FONT_TEXTURE_SIZE, c - FIRST_CHAR, x, y, quad, true);
            }
        }
        return x[0];
    }
}
