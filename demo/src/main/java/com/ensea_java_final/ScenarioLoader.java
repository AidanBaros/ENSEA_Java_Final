package com.ensea_java_final;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ScenarioLoader {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Lists all .json files in the given directory (without extension filtering).
     * @param directory Path to directory containing scenario files.
     * @return List of filenames (not including directory path).
     */
    public static List<String> listJsonFiles(String directory) {
        List<String> files = new ArrayList<>();
        Path dir = Paths.get(directory);
        if (!Files.isDirectory(dir)) {
            return files;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.json")) {
            for (Path p : stream) {
                files.add(p.getFileName().toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to list scenario files in " + directory, e);
        }
        return files;
    }

    /**
     * Loads a scenario JSON file and constructs Body instances.
     * @param filepath Path to the scenario JSON file.
     * @return List of Bodies parsed from the file.
     */
    public static List<Body> load(String filepath) {
        try {
            JsonNode root = mapper.readTree(Paths.get(filepath).toFile());
            List<Body> bodies = new ArrayList<>();
            JsonNode arr = root.get("bodies");
            if (arr != null && arr.isArray()) {
                for (JsonNode n : arr) {
                    Body.Builder builder = new Body.Builder()
                        .mass(n.get("mass").asDouble())
                        .size(n.get("size").asDouble());

                    JsonNode pos = n.get("position");
                    if (pos != null && pos.isArray() && pos.size() >= 2) {
                        builder.position(pos.get(0).asDouble(), pos.get(1).asDouble());
                    }
                    JsonNode vel = n.get("velocity");
                    if (vel != null && vel.isArray() && vel.size() >= 2) {
                        builder.velocity(vel.get(0).asDouble(), vel.get(1).asDouble());
                    }
                    if (n.has("fixed") && n.get("fixed").asBoolean()) {
                        builder.fixed(true);
                    }
                    JsonNode color = n.get("color");
                    if (color != null && color.isArray() && color.size() >= 3) {
                        builder.color(
                            (float) color.get(0).asDouble(),
                            (float) color.get(1).asDouble(),
                            (float) color.get(2).asDouble()
                        );
                    }
                    if (n.has("texturePath")) {
                        builder.texture(n.get("texturePath").asText());
                    }
                    bodies.add(builder.build());
                }
            }
            return bodies;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load scenario from " + filepath, e);
        }
    }

    /**
     * Retrieves the simulation name from the JSON file, defaulting if absent.
     * @param filepath Path to the scenario JSON file.
     * @return Simulation name or a default string.
     */
    public static String getSimulationName(String filepath) {
        try {
            JsonNode root = mapper.readTree(Paths.get(filepath).toFile());
            JsonNode nameNode = root.get("simulationName");
            if (nameNode != null && nameNode.isTextual()) {
                return nameNode.asText();
            }
        } catch (IOException e) {
            // ignore and fall through
        }
        return "Physics Simulation";
    }
}
