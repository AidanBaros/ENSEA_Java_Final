package com.ensea_java_final;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;

/**
 * Utility class for loading, caching, and disposing of textures.
 */
public class TextureUtils {
    private static final Map<String, Integer> textureCache = new ConcurrentHashMap<>();

    /**
     * Loads a texture from the given file path, or returns a cached texture ID if already loaded.
     * @param path Filesystem path to the image file.
     * @return OpenGL texture ID.
     */
    public static int loadTexture(String path) {
        return textureCache.computeIfAbsent(path, TextureUtils::loadTextureInternal);
    }

    /**
     * Frees all loaded textures and clears the cache. Call on shutdown.
     */
    public static void disposeAll() {
        for (int texId : textureCache.values()) {
            glDeleteTextures(texId);
        }
        textureCache.clear();
    }

    /**
     * Internal helper: loads a new texture from disk.
     */
    private static int loadTextureInternal(String path) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Flip vertically so textures are upright in OpenGL
            STBImage.stbi_set_flip_vertically_on_load(true);

            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            ByteBuffer image = STBImage.stbi_load(path, w, h, comp, 4);
            if (image == null) {
                throw new RuntimeException("Failed to load texture '" + path + "': " + STBImage.stbi_failure_reason());
            }

            int texId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, texId);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w.get(0), h.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            glBindTexture(GL_TEXTURE_2D, 0);
            STBImage.stbi_image_free(image);

            return texId;
        }
    }
}
