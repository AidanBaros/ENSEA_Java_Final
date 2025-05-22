package com.ensea_java_final;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class Body {
    // Instance fields
    private Double mass;
    private Double size;
    private Vector2D position;
    private Vector2D velocity;
    private Vector2D acceleration = new Vector2D(0.0, 0.0);
    private Boolean fixed;
    private float r = 1.0f, g = 0.0f, b = 0.0f;
    private Integer textureId = null;
    private String texturePath = null;
    private Boolean isColliding;

    // Private constructor
    private Body(Builder builder) {
        this.mass = builder.mass;
        this.size = builder.size;
        this.position = new Vector2D(builder.x, builder.y);
        this.velocity = new Vector2D(builder.vx, builder.vy);
        this.fixed = builder.fixed;
        this.r = builder.r;
        this.g = builder.g;
        this.b = builder.b;
        if (builder.texturePath != null && !builder.texturePath.isEmpty()) {
            this.texturePath = builder.texturePath;
            this.textureId = loadTexture(builder.texturePath);
        }
    }

    // Getters
    public Double getMass()               { return mass; }
    public Double getSize()               { return size; }
    public Vector2D getPosition()         { return position; }
    public Vector2D getVelocity()         { return velocity; }
    public Vector2D getAcceleration()     { return acceleration; }
    public Boolean isFixed()              { return fixed; }
    public Integer getTextureId()         { return textureId; }
    public String getTexturePath()        { return texturePath; }
    public Boolean isColliding()          { return isColliding; }

    // Setters
    public void setMass(Double mass)                  { this.mass = mass; }
    public void setSize(Double size)                  { this.size = size; }
    public void setPosition(Vector2D position)        { this.position = position; }
    public void setVelocity(Vector2D velocity)        { this.velocity = velocity; }
    public void setAcceleration(Vector2D acceleration){ this.acceleration = acceleration; }
    public void setFixed(Boolean fixed)               { this.fixed = fixed; }
    public void setColor(float r, float g, float b)   { this.r = r; this.g = g; this.b = b; }
    public void setColliding(Boolean colliding)       { this.isColliding = colliding; }

    public void setTexture(String path) {
        if (this.textureId != null) {
            glDeleteTextures(this.textureId);
        }
        if (path != null && !path.isEmpty()) {
            this.textureId = loadTexture(path);
            this.texturePath = path;
        } else {
            this.textureId = null;
            this.texturePath = null;
        }
    }

    // Movement and rendering
    public void move(Vector2D newPos) {
        if (!fixed) {
            this.position = newPos;
        }
    }

    public void draw() {
        glEnable(GL_MULTISAMPLE);
        if (textureId != null) {
            drawTexturedCircle(position.x.floatValue(), position.y.floatValue(), size.floatValue(), 64, textureId);
        } else {
            glColor3f(r, g, b);
            drawCircle(position.x.floatValue(), position.y.floatValue(), size.floatValue(), 64);
        }
        glDisable(GL_MULTISAMPLE);
    }

    // Static drawing helpers
    public static void drawCircle(float cx, float cy, float radius, int segments) {
        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(cx, cy);
        double step = 2.0 * Math.PI / segments;
        for (int i = 0; i <= segments; i++) {
            double angle = i * step;
            glVertex2f(
                cx + (float)Math.cos(angle) * radius,
                cy + (float)Math.sin(angle) * radius
            );
        }
        glEnd();
    }

    public static void drawTexturedCircle(float cx, float cy, float radius, int segments, int textureId) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glColor3f(1f, 1f, 1f);

        glBegin(GL_TRIANGLE_FAN);
        glTexCoord2f(0.5f, 0.5f);
        glVertex2f(cx, cy);
        double step = 2.0 * Math.PI / segments;
        for (int i = 0; i <= segments; i++) {
            double angle = i * step;
            float x = cx + (float)Math.cos(angle) * radius;
            float y = cy + (float)Math.sin(angle) * radius;
            glTexCoord2f(0.5f + 0.5f * (float)Math.cos(angle),
                         0.5f + 0.5f * (float)Math.sin(angle));
            glVertex2f(x, y);
        }
        glEnd();

        glBindTexture(GL_TEXTURE_2D, 0);
        glDisable(GL_TEXTURE_2D);
    }

    private static int loadTexture(String path) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            STBImage.stbi_set_flip_vertically_on_load(true);

            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            ByteBuffer image = STBImage.stbi_load(path, w, h, comp, 4);
            if (image == null) {
                throw new RuntimeException("Failed to load texture: " + path);
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

    // Builder for Body
    public static class Builder {
        private Double mass;
        private Double size;
        private Double x, y;
        private Double vx = 0.0, vy = 0.0;
        private Boolean fixed = false;
        private float r = 1.0f, g = 1.0f, b = 1.0f;
        private String texturePath;
        private Boolean isColliding = false;

        public Builder mass(Double mass)               { this.mass = mass; return this; }
        public Builder size(Double size)               { this.size = size; return this; }
        public Builder position(Double x, Double y)    { this.x = x; this.y = y; return this; }
        public Builder velocity(Double vx, Double vy)  { this.vx = vx; this.vy = vy; return this; }
        public Builder color(float r, float g, float b){ this.r = r; this.g = g; this.b = b; return this; }
        public Builder texture(String path)            { this.texturePath = path; return this; }
        public Builder fixed(Boolean fixed)            { this.fixed = fixed; return this; }

        public Body build() {
            if (mass == null || size == null || x == null || y == null) {
                throw new IllegalStateException("Mass, size, and position are required.");
            }
            return new Body(this);
        }
    }
}