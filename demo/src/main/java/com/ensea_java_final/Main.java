package com.ensea_java_final;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.opengl.GL.createCapabilities;

import java.util.ArrayList;
import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.BufferUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

enum MenuState {
    MENU, SIMULATION
}

public class Main {
    private static final int FONT_TEXTURE_SIZE = 1024;

    private long window;
    private ArrayList<Body> bodies = new ArrayList<>();
    private PhysicsEngine physicsEngine = new PhysicsEngine(bodies);

    private MenuState currentState = MenuState.MENU;
    private int selectedIndex = 0;
    private final ArrayList<String> scenarioFiles = new ArrayList<>();

    private static STBTTBakedChar.Buffer cdata;
    private static int fontTex;

    public static void main(String[] args) {
        new Main().run();
    }

    private void initFont() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("fonts/Roboto-Regular.ttf")) {
            if (is == null) {
                throw new IOException("Font file not found.");
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int read;
            while ((read = is.read(buf)) != -1) {
                baos.write(buf, 0, read);
            }

            ByteBuffer ttf = BufferUtils
                .createByteBuffer(baos.size())
                .put(baos.toByteArray());
            ttf.flip();

            ByteBuffer bitmap = BufferUtils.createByteBuffer(FONT_TEXTURE_SIZE * FONT_TEXTURE_SIZE);
            cdata = STBTTBakedChar.malloc(96);
            STBTruetype.stbtt_BakeFontBitmap(ttf, 128, bitmap, FONT_TEXTURE_SIZE, FONT_TEXTURE_SIZE, 32, cdata);

            fontTex = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, fontTex);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, FONT_TEXTURE_SIZE, FONT_TEXTURE_SIZE, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_RED);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_RED);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_RED);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_RED);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            System.out.println("Font loaded successfully");
        } catch (IOException e) {
            System.err.println("Failed to load font: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void run() {
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_ANY_PROFILE);

        WindowManager.init(800, 600, "Physics Simulation");
        window = WindowManager.getWindow();
        createCapabilities();

        initFont();
        loadScenarioFiles();
        loop();

        physicsEngine.shutdownExecutor();
        WindowManager.cleanup();
    }

    private void loadScenarioFiles() {
        File folder = new File("scenario");
        File[] files = folder.listFiles((d, n) -> n.endsWith(".json"));
        if (files != null) {
            for (File f : files) {
                scenarioFiles.add(f.getName());
            }
        }
    }

    private void loadScenario(String path) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new File(path));
            bodies.clear();

            for (JsonNode node : root.get("bodies")) {
                Body.Builder builder = new Body.Builder()
                    .mass(node.get("mass").asDouble())
                    .size(node.get("size").asDouble())
                    .position(node.get("position").get(0).asDouble(), node.get("position").get(1).asDouble())
                    .velocity(node.get("velocity").get(0).asDouble(), node.get("velocity").get(1).asDouble())
                    .fixed(node.has("fixed") && node.get("fixed").asBoolean());

                if (node.has("color")) {
                    builder.color(
                        (float) node.get("color").get(0).asDouble(),
                        (float) node.get("color").get(1).asDouble(),
                        (float) node.get("color").get(2).asDouble()
                    );
                }
                if (node.has("texturePath")) {
                    builder.texture(node.get("texturePath").asText());
                }

                bodies.add(builder.build());
            }

            physicsEngine = new PhysicsEngine(bodies);
            String gravity = root.has("gravityType") ? root.get("gravityType").asText() : "game";
            physicsEngine.setGravityType(gravity);

            String title = root.has("simulationName") ? root.get("simulationName").asText() : "Physics Simulation";
            glfwSetWindowTitle(window, title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glEnable(GL_TEXTURE_2D);
            glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

            glfwPollEvents();
            glClear(GL_COLOR_BUFFER_BIT);

            if (currentState == MenuState.MENU) {
                handleMenuInput();
                renderMenu();
            } else {
                handleSimulationInput();
                physicsEngine.update();
                for (Body body : bodies) {
                    body.draw();
                }
            }

            WindowManager.update();
        }
    }

    private void handleMenuInput() {
        if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
            selectedIndex = (selectedIndex + 1) % (scenarioFiles.size() + 1);
            sleep(150);
        }
        if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
            selectedIndex = (selectedIndex - 1 + scenarioFiles.size() + 1) % (scenarioFiles.size() + 1);
            sleep(150);
        }
        if (glfwGetKey(window, GLFW_KEY_ENTER) == GLFW_PRESS) {
            if (selectedIndex == scenarioFiles.size()) {
                glfwSetWindowShouldClose(window, true);
            } else {
                loadScenario("scenario/" + scenarioFiles.get(selectedIndex));
                currentState = MenuState.SIMULATION;
            }
            sleep(200);
        }
    }

    private void handleSimulationInput() {
        if (glfwGetKey(window, GLFW_KEY_EQUAL) == GLFW_PRESS
         || glfwGetKey(window, GLFW_KEY_KP_ADD) == GLFW_PRESS) {
            double current = physicsEngine.getTimeScale();
            physicsEngine.setTimeScale(Math.min(current + 0.1, 5.0));
            System.out.println("Time Scale: " + physicsEngine.getTimeScale());
        }
        if (glfwGetKey(window, GLFW_KEY_MINUS) == GLFW_PRESS
         || glfwGetKey(window, GLFW_KEY_KP_SUBTRACT) == GLFW_PRESS) {
            double current = physicsEngine.getTimeScale();
            physicsEngine.setTimeScale(Math.max(current - 0.1, 1.0));
            System.out.println("Time Scale: " + physicsEngine.getTimeScale());
        }
        if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            currentState = MenuState.MENU;
            sleep(200);
        }
    }

    private void renderMenu() {
        int[] w = new int[1], h = new int[1];
        glfwGetFramebufferSize(window, w, h);
        float fw = w[0], fh = h[0];

        glEnable(GL_TEXTURE_2D);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

        int startY = 100 * 4;
        int lineH  =  30 * 4;

        for (int i = 0; i < scenarioFiles.size(); i++) {
            boolean sel = (i == selectedIndex);
            float r = sel ? 1f : 1f;
            float g = sel ? 0f : 1f;
            float b = sel ? 0f : 1f;
            drawTextCentered(fw / 2, startY + i * lineH, "Start: " + scenarioFiles.get(i), r, g, b);
        }

        boolean quitSel = (selectedIndex == scenarioFiles.size());
        drawTextCentered(
            fw / 2,
            startY + scenarioFiles.size() * lineH,
            "Quit",
            quitSel ? 1f : 1f,
            quitSel ? 0f : 1f,
            quitSel ? 0f : 1f
        );
    }

    private void drawTextCentered(float centerX, float y, String text, float r, float g, float b) {
        if (cdata == null) {
            return;
        }

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glBindTexture(GL_TEXTURE_2D, fontTex);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
        glColor3f(r, g, b);

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
        float[] xPos = { startX }, yPos = { y };

        try (MemoryStack stack = MemoryStack.stackPush()) {
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
            for (char c : text.toCharArray()) {
                if (c < 32 || c >= 128) continue;
                STBTruetype.stbtt_GetBakedQuad(
                    cdata, FONT_TEXTURE_SIZE, FONT_TEXTURE_SIZE,
                    c - 32, xPos, yPos, quad, true
                );
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

        glDisable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
    }

    private float calculateTextWidth(String text) {
        float[] x = {0f}, y = {0f};
        try (MemoryStack stack = MemoryStack.stackPush()) {
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
            for (char c : text.toCharArray()) {
                if (c < 32 || c >= 128) continue;
                STBTruetype.stbtt_GetBakedQuad(
                    cdata, FONT_TEXTURE_SIZE, FONT_TEXTURE_SIZE,
                    c - 32, x, y, quad, true
                );
            }
        }
        return x[0];
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }
}