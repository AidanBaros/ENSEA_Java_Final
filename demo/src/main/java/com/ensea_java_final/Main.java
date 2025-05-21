package com.ensea_java_final;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import java.util.ArrayList;
import static org.lwjgl.opengl.GL.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;

public class Main {
    private long window;

    private ArrayList<Body> bodies = new ArrayList<Body>();
    private PhysicsEngine physicsEngine = new PhysicsEngine(bodies);

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        String scenarioPath = "scenario/orbit_fixed.json";
        WindowManager.init(800, 600, "Physics Simulation");
        window = WindowManager.getWindow();

        createCapabilities();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new File(scenarioPath));

            bodies.clear();

            for (JsonNode bodyNode : root.get("bodies")) {
                Body.Builder builder = new Body.Builder()
                    .mass(bodyNode.get("mass").asDouble())
                    .size(bodyNode.get("size").asDouble())
                    .position(
                        bodyNode.get("position").get(0).asDouble(),
                        bodyNode.get("position").get(1).asDouble()
                    )
                    .velocity(
                        bodyNode.get("velocity").get(0).asDouble(),
                        bodyNode.get("velocity").get(1).asDouble()
                    )
                    .fixed(bodyNode.has("fixed") && bodyNode.get("fixed").asBoolean());
                if (bodyNode.has("color")) {
                    builder.color(
                        (float) bodyNode.get("color").get(0).asDouble(),
                        (float) bodyNode.get("color").get(1).asDouble(),
                        (float) bodyNode.get("color").get(2).asDouble()
                    );
                }
                if (bodyNode.has("texturePath")) {
                    builder.texture(bodyNode.get("texturePath").asText());
                }
                bodies.add(builder.build());
            }

            physicsEngine = new PhysicsEngine(bodies);

            if (root.has("gravityType")) {
                String gravityType = root.get("gravityType").asText();
                physicsEngine.setGravityType(gravityType);
            } else {
                physicsEngine.setGravityType("game");
            }

            if (root.has("simulationName")) {
                String simulationName = root.get("simulationName").asText();
                glfwSetWindowTitle(window, simulationName);
            } else {
                glfwSetWindowTitle(window, "Physics Simulation");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        loop();


        physicsEngine.shutdownExecutor();
        WindowManager.cleanup();
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();
            handleInput();

            glClear(GL_COLOR_BUFFER_BIT);

            physicsEngine.update();

            for (Body body : bodies) {
                body.draw();
            }

            WindowManager.update();
        }
    }

    private void handleInput() {
        if (glfwGetKey(window, GLFW_KEY_EQUAL) == GLFW_PRESS) { // '+' key
            Double current = physicsEngine.getTimeScale();
            physicsEngine.setTimeScale(Math.min(current + 0.1, 5.0));
            System.out.println("Time Scale: " + physicsEngine.getTimeScale());
        }

        if (glfwGetKey(window, GLFW_KEY_MINUS) == GLFW_PRESS) { // '-' key
            Double current = physicsEngine.getTimeScale();
            physicsEngine.setTimeScale(Math.max(current - 0.1, 0.0));
            System.out.println("Time Scale: " + physicsEngine.getTimeScale());
        }
    }
}
