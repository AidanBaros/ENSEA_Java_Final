package com.ensea_java_final;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class Body {
    private Double mass, size;
    private float r = 1.0f, g = 0.0f, b = 0.0f;
    private Vector2D position;
    private Vector2D velocity;
    private Vector2D acceleration;
    private Boolean fixed;
    private Integer textureId = null;
    private String texturePath = null;
    private Boolean isColliding;

    public enum ShapeType { CIRCLE, RECTANGLE, HOLLOW_CIRCLE }
    private ShapeType shape = ShapeType.CIRCLE;
    // Rectangle properties
    private Double width = null, height = null;
    // Hollow circle properties
    private Double innerRadius = null, missingAngleStart = null, missingAngleExtent = null;
    // Rotation (in radians)
    private Double rotation = 0.0;
    // Environment flag
    private Boolean isEnvironment = false;

    // Private constructor: enforce use of Builder
    private Body(Builder builder) {
        this.mass = builder.mass;
        this.size = builder.size;
        this.position = new Vector2D(builder.x, builder.y);
        this.velocity = new Vector2D(builder.vx, builder.vy);
        this.r = builder.r;
        this.g = builder.g;
        this.b = builder.b;
        this.acceleration = new Vector2D(0.0, 0.0);
        this.fixed = builder.fixed;
        this.shape = builder.shape;
        this.width = builder.width;
        this.height = builder.height;
        this.innerRadius = builder.innerRadius;
        this.missingAngleStart = builder.missingAngleStart;
        this.missingAngleExtent = builder.missingAngleExtent;
        this.rotation = builder.rotation;
        this.isEnvironment = builder.isEnvironment;
        if (builder.texturePath != null) {
            this.texturePath = builder.texturePath;
            this.textureId = loadTexture(builder.texturePath);
        }
    }

    // --- Getters ---
    public Double getMass() { return mass; }
    public Double getSize() { return size; }
    public Vector2D getPosition() { return position; }
    public Vector2D getVelocity() { return velocity; }
    public Vector2D getAcceleration() { return acceleration; }
    public Boolean isFixed() { return fixed; }
    public Integer getTextureId() { return textureId; }
    public String getTexturePath() { return texturePath; }
    public Boolean isColliding() {return isColliding; }
    public ShapeType getShape() { return shape; }
    public Double getWidth() { return width; }
    public Double getHeight() { return height; }
    public Double getInnerRadius() { return innerRadius; }
    public Double getMissingAngleStart() { return missingAngleStart; }
    public Double getMissingAngleExtent() { return missingAngleExtent; }
    public Double getRotation() { return rotation; }
    public Boolean isEnvironment() { return isEnvironment; }

    // --- Setters ---
    public void setMass(Double mass) {this.mass = mass;}
    public void setSize(Double size) {this.size = size;}
    public void setPosition(Vector2D position) {this.position = position;}
    public void setVelocity(Vector2D velocity) {this.velocity = velocity;}
    public void setAcceleration(Vector2D acceleration) {this.acceleration = acceleration;}
    public void setFixed(Boolean fixed) {this.fixed = fixed;}
    public void setColor(float r, float g, float b) {this.r = r; this.g = g; this.b = b;}
    public void setTexture(String path) {
        if (this.textureId != null) {
            glDeleteTextures(this.textureId);
        }
        this.textureId = loadTexture(path);
        this.texturePath = path;
    }
    public void setColliding(Boolean colliding) {this.isColliding = colliding; }
    public void setShape(ShapeType shape) { this.shape = shape; }
    public void setWidth(Double width) { this.width = width; }
    public void setHeight(Double height) { this.height = height; }
    public void setInnerRadius(Double innerRadius) { this.innerRadius = innerRadius; }
    public void setMissingAngleStart(Double start) { this.missingAngleStart = start; }
    public void setMissingAngleExtent(Double extent) { this.missingAngleExtent = extent; }
    public void setRotation(Double rotation) { this.rotation = rotation; }
    public void setEnvironment(Boolean env) { this.isEnvironment = env; }

    // --- Functions ---
    public void move(Vector2D position){
        if (!fixed && !isEnvironment){
            this.position = position;
        }
    }

    public void draw() {
        glEnable(GL_MULTISAMPLE);
        glPushMatrix();
        glTranslated(position.x, position.y, 0);
        glRotated(Math.toDegrees(rotation), 0, 0, 1);
        switch (shape) {
            case RECTANGLE:
                glColor3f(r, g, b);
                drawRectangle(0, 0, width.floatValue(), height.floatValue());
                break;
            case HOLLOW_CIRCLE:
                glColor3f(r, g, b);
                drawHollowCircle(0, 0, size.floatValue(), innerRadius.floatValue(), 64, missingAngleStart, missingAngleExtent);
                break;
            case CIRCLE:
            default:
                if (textureId != null) {
                    drawTexturedCircle(0, 0, size.floatValue(), 64, textureId);
                } else {
                    glColor3f(r, g, b);
                    drawCircle(0, 0, size.floatValue(), 64);
                }
                break;
        }
        glPopMatrix();
        glDisable(GL_MULTISAMPLE);
    }

    public static void drawCircle(float cx, float cy, float r, int segments) {
        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(cx, cy);
        double step = 2.0 * Math.PI / segments;
        //redraw
        for (int i = 0; i <= segments; i++) {
            double angle = i * step;
            glVertex2f(
                cx + (float)Math.cos(angle) * r,
                cy + (float)Math.sin(angle) * r
            );
        }
        glEnd();
    }

    public static void drawTexturedCircle(float cx, float cy, float r, int segments, int textureId) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glColor3f(1, 1, 1);

        glBegin(GL_TRIANGLE_FAN);
        glTexCoord2f(0.5f, 0.5f);
        glVertex2f(cx, cy);
        double step = 2.0 * Math.PI / segments;
        for (int i = 0; i <= segments; i++) {
            double angle = i * step;
            float x = cx + (float)Math.cos(angle) * r;
            float y = cy + (float)Math.sin(angle) * r;
            float u = 0.5f + 0.5f * (float)Math.cos(angle);
            float v = 0.5f + 0.5f * (float)Math.sin(angle);
            glTexCoord2f(u, v);
            glVertex2f(x, y);
        }
        glEnd();

        glBindTexture(GL_TEXTURE_2D, 0);
        glDisable(GL_TEXTURE_2D);
    }

    public static void drawRectangle(float cx, float cy, float w, float h) {
        glBegin(GL_QUADS);
        glVertex2f(cx - w/2, cy - h/2);
        glVertex2f(cx + w/2, cy - h/2);
        glVertex2f(cx + w/2, cy + h/2);
        glVertex2f(cx - w/2, cy + h/2);
        glEnd();
    }

    public static void drawHollowCircle(float cx, float cy, float outerR, float innerR, int segments, Double missingStart, Double missingExtent) {
        double start = missingStart != null ? missingStart : 0.0;
        double extent = missingExtent != null ? missingExtent : 0.0;
        double step = 2.0 * Math.PI / segments;
        double end = 2 * Math.PI;
        if (extent > 0) {
            end = start + (2 * Math.PI - extent);
        }
        glBegin(GL_TRIANGLE_STRIP);
        for (double angle = start; angle <= end; angle += step) {
            glVertex2f((float)Math.cos(angle) * outerR, (float)Math.sin(angle) * outerR);
            glVertex2f((float)Math.cos(angle) * innerR, (float)Math.sin(angle) * innerR);
        }
        glEnd();
    }

    private static int loadTexture(String path) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Flip image vertically so textures appear right-side up in OpenGL
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

    // --- Builder ---
    public static class Builder {
        private Double mass, size;
        private Double x, y;
        private Double vx = 0.0, vy = 0.0; // default to 0 if not set
        private Boolean fixed = false;
        private float r = 1.0f, g = 1.0f, b = 1.0f; // default to white if not set
        private String texturePath = null;
        private Boolean isColliding = false;
        private ShapeType shape = ShapeType.CIRCLE;
        private Double width = null, height = null;
        private Double innerRadius = null, missingAngleStart = null, missingAngleExtent = null;
        private Double rotation = 0.0;
        private Boolean isEnvironment = false;

        public Builder mass(Double mass) {
            this.mass = mass;
            return this;
        }

        public Builder size(Double size) {
            this.size = size;
            return this;
        }

        public Builder position(Double x, Double y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder velocity(Double vx, Double vy) {
            this.vx = vx;
            this.vy = vy;
            return this;
        }

        public Builder color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
            return this;
        }

        public Builder texture(String path) {
            this.texturePath = path;
            return this;
        }

        public Builder fixed(Boolean fixed) {
            this.fixed = fixed;
            return this;
        }

        public Builder shape(ShapeType shape) {
            this.shape = shape;
            return this;
        }
        public Builder rectangle(Double width, Double height) {
            this.shape = ShapeType.RECTANGLE;
            this.width = width;
            this.height = height;
            return this;
        }
        public Builder hollowCircle(Double innerRadius, Double missingAngleStart, Double missingAngleExtent) {
            this.shape = ShapeType.HOLLOW_CIRCLE;
            this.innerRadius = innerRadius;
            this.missingAngleStart = missingAngleStart;
            this.missingAngleExtent = missingAngleExtent;
            return this;
        }
        public Builder rotation(Double rotation) {
            this.rotation = rotation;
            return this;
        }
        public Builder environment(Boolean env) {
            this.isEnvironment = env;
            this.fixed = true;
            return this;
        }

        public Body build() {
            if (mass == null || size == null || x == null || y == null) {
                throw new IllegalStateException("Mass, Size, and Position must be set.");
            }

            Body b = new Body(this);
            return b;
        }
    }
}
