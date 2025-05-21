package com.ensea_java_final;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL33.*; // For swizzle constants and modern OpenGL
import static org.lwjgl.opengl.GL.createCapabilities;

import java.util.ArrayList;
import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

enum MenuState {
    MENU, SIMULATION
}

public class Main {
    private long window;

    private ArrayList<Body> bodies = new ArrayList<>();
    private PhysicsEngine physicsEngine = new PhysicsEngine(bodies);

    private MenuState currentState = MenuState.MENU;
    private int selectedIndex = 0;
    private final ArrayList<String> scenarioFiles = new ArrayList<>();

    private static final int FONT_TEXTURE_SIZE = 1024;
    private static STBTTBakedChar.Buffer cdata;
    private static int fontTex;

    private void initFont() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("fonts/Roboto-Regular.ttf");
            if (is == null) throw new IOException("Font file not found.");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024]; int r;
            while ((r = is.read(buf)) != -1) baos.write(buf, 0, r);
            ByteBuffer ttf = BufferUtils.createByteBuffer(baos.size()).put(baos.toByteArray());
            ttf.flip();

            ByteBuffer bitmap = BufferUtils.createByteBuffer(FONT_TEXTURE_SIZE * FONT_TEXTURE_SIZE);
            cdata = STBTTBakedChar.malloc(96);
            STBTruetype.stbtt_BakeFontBitmap(ttf, 32, bitmap, FONT_TEXTURE_SIZE, FONT_TEXTURE_SIZE, 32, cdata);

            fontTex = glGenTextures(); glBindTexture(GL_TEXTURE_2D, fontTex);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, FONT_TEXTURE_SIZE, FONT_TEXTURE_SIZE, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_ONE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_ONE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_ONE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_RED);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            System.out.println("Font loaded successfully");
        } catch (IOException e) {
            System.err.println("Failed to load font: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        WindowManager.init(800, 600, "Physics Simulation");
        window = WindowManager.getWindow();
        createCapabilities();
        initFont();
        loadScenarioFiles();
        loop();
        physicsEngine.shutdownExecutor();
        WindowManager.cleanup();
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();
            glClear(GL_COLOR_BUFFER_BIT);

            if (currentState == MenuState.MENU) {
                handleMenuInput();
                renderMenu();
            } else {
                handleSimulationInput();
                physicsEngine.update();
                for (Body b : bodies) b.draw();
            }

            WindowManager.update();
        }
    }

    private void loadScenarioFiles() {
        File folder = new File("scenario");
        File[] files = folder.listFiles((d, n) -> n.endsWith(".json"));
        if (files != null) for (File f : files) scenarioFiles.add(f.getName());
    }

    private void handleMenuInput() {
        if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) { selectedIndex = (selectedIndex+1)%(scenarioFiles.size()+1); sleep(150);} 
        if (glfwGetKey(window, GLFW_KEY_UP)   == GLFW_PRESS) { selectedIndex = (selectedIndex-1+scenarioFiles.size()+1)%(scenarioFiles.size()+1); sleep(150);} 
        if (glfwGetKey(window, GLFW_KEY_ENTER)== GLFW_PRESS) {
            if (selectedIndex == scenarioFiles.size()) glfwSetWindowShouldClose(window,true);
            else { loadScenario("scenario/"+scenarioFiles.get(selectedIndex)); currentState = MenuState.SIMULATION; }
            sleep(200);
        }
    }

    private void renderMenu() {
        // Get framebuffer size
        int[] w = new int[1], h = new int[1];
        glfwGetFramebufferSize(window, w, h);
        float fw = w[0], fh = h[0];

        // Draw grid for layout debugging
        int gridCount = 10;
        glColor3f(0.3f, 0.3f, 0.3f);
        glLineWidth(1.0f);
        glBegin(GL_LINES);
        for (int i = 1; i < gridCount; i++) {
            float x = i * (fw / gridCount);
            glVertex2f(x, 0);
            glVertex2f(x, fh);
            float y = i * (fh / gridCount);
            glVertex2f(0, y);
            glVertex2f(fw, y);
        }
        glEnd();

        // Draw center crosshair in green
        glColor3f(0f, 1f, 0f);
        glLineWidth(2.0f);
        glBegin(GL_LINES);
            glVertex2f(fw/2, 0);
            glVertex2f(fw/2, fh);
            glVertex2f(0, fh/2);
            glVertex2f(fw, fh/2);
        glEnd();

        // Draw menu entries
        glLineWidth(1.0f);
        int startY = 100;
        int lineH = 30;
        for (int i = 0; i < scenarioFiles.size(); i++) {
            glColor3f(i == selectedIndex ? 1f : 1f, i == selectedIndex ? 0f : 1f, 0f);
            drawTextCentered(fw/2, startY + i * lineH, "Start: " + scenarioFiles.get(i));
        }
        // "Quit" option
        glColor3f(selectedIndex == scenarioFiles.size() ? 1f : 1f,
                  selectedIndex == scenarioFiles.size() ? 0f : 1f,
                  0f);
        drawTextCentered(fw/2, startY + scenarioFiles.size() * lineH, "Quit");
    }

    private void drawTextCentered(float centerX, float y, String text) {
        if (cdata == null) return;
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glBindTexture(GL_TEXTURE_2D, fontTex);

        // Projection for pixel coordinates
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        int[] w = new int[1], h = new int[1];
        glfwGetFramebufferSize(window, w, h);
        glOrtho(0, w[0], h[0], 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        float textWidth = calculateTextWidth(text);
        float startX = centerX - textWidth / 2f;
        float[] xPos = { startX };
        float[] yPos = { y };
        try (MemoryStack stack = MemoryStack.stackPush()) {
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
            for (char c : text.toCharArray()) {
                if (c < 32 || c >= 128) continue;
                STBTruetype.stbtt_GetBakedQuad(cdata, FONT_TEXTURE_SIZE, FONT_TEXTURE_SIZE, c - 32, xPos, yPos, quad, true);
                glBegin(GL_QUADS);
                glTexCoord2f(quad.s0(), quad.t0()); glVertex2f(quad.x0(), quad.y0());
                glTexCoord2f(quad.s1(), quad.t0()); glVertex2f(quad.x1(), quad.y0());
                glTexCoord2f(quad.s1(), quad.t1()); glVertex2f(quad.x1(), quad.y1());
                glTexCoord2f(quad.s0(), quad.t1()); glVertex2f(quad.x0(), quad.y1());
                glEnd();
            }
        }

        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glDisable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
    }

    private float calculateTextWidth(String text) {
        float[] x = {0f}, y = {0f};
        try (MemoryStack stack = MemoryStack.stackPush()) {
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
            for (char c : text.toCharArray()) {
                if (c < 32 || c >= 128) continue;
                STBTruetype.stbtt_GetBakedQuad(cdata, FONT_TEXTURE_SIZE, FONT_TEXTURE_SIZE, c - 32, x, y, quad, true);
            }
        }
        return x[0];
    }

    private void handleSimulationInput() {
        if (glfwGetKey(window, GLFW_KEY_EQUAL) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_KP_ADD) == GLFW_PRESS) {
            double current = physicsEngine.getTimeScale();
            physicsEngine.setTimeScale(Math.min(current + 0.1, 5.0));
            System.out.println("Time Scale: " + physicsEngine.getTimeScale());
        }
        if (glfwGetKey(window, GLFW_KEY_MINUS) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_KP_SUBTRACT) == GLFW_PRESS) {
            double current = physicsEngine.getTimeScale();
            physicsEngine.setTimeScale(Math.max(current - 0.1, 1.0));
            System.out.println("Time Scale: " + physicsEngine.getTimeScale());
        }
        if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            currentState = MenuState.MENU;
            sleep(200);
        }
    }

    private void loadScenario(String path) {
        try {
            ObjectMapper m = new ObjectMapper();
            JsonNode r = m.readTree(new File(path));
            bodies.clear();
            for (JsonNode n : r.get("bodies")) {
                Body.Builder b = new Body.Builder()
                    .mass(n.get("mass").asDouble())
                    .size(n.get("size").asDouble())
                    .position(n.get("position").get(0).asDouble(), n.get("position").get(1).asDouble())
                    .velocity(n.get("velocity").get(0).asDouble(), n.get("velocity").get(1).asDouble())
                    .fixed(n.has("fixed") && n.get("fixed").asBoolean());
                if (n.has("color")) b.color((float)n.get("color").get(0).asDouble(), (float)n.get("color").get(1).asDouble(), (float)n.get("color").get(2).asDouble());
                if (n.has("texturePath")) b.texture(n.get("texturePath").asText());
                bodies.add(b.build());
            }
            physicsEngine = new PhysicsEngine(bodies);
            physicsEngine.setGravityType(r.has("gravityType") ? r.get("gravityType").asText() : "game");
            glfwSetWindowTitle(window, r.has("simulationName") ? r.get("simulationName").asText() : "Physics Simulation");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
