package com.ensea_java_final;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;

public class Body {
    private final double mass;
    private final double size;
    private Vector2D position;
    private Vector2D velocity;
    private Vector2D acceleration = new Vector2D(0.0, 0.0);
    private final boolean fixed;

    private float r = 1.0f, g = 0.0f, b = 0.0f;
    private int textureId = 0;
    private boolean colliding = false;

    private Body(Builder builder) {
        this.mass = builder.mass;
        this.size = builder.size;
        this.position = new Vector2D(builder.x, builder.y);
        this.velocity = new Vector2D(builder.vx, builder.vy);
        this.fixed = builder.fixed;
        this.r = builder.r;
        this.g = builder.g;
        this.b = builder.b;

        if (builder.texturePath != null) {
            this.textureId = TextureUtils.loadTexture(builder.texturePath);
        }
    }

    // Getters
    public double getMass()         { return mass; }
    public double getSize()         { return size; }
    public Vector2D getPosition()   { return position; }
    public Vector2D getVelocity()   { return velocity; }
    public Vector2D getAcceleration(){ return acceleration; }
    public boolean isFixed()        { return fixed; }
    public boolean isColliding()    { return colliding; }

    // Setters for PhysicsEngine
    public void setPosition(Vector2D p)     { this.position = p; }
    public void setVelocity(Vector2D v)     { this.velocity = v; }
    public void setAcceleration(Vector2D a) { this.acceleration = a; }
    public void setColliding(boolean c)     { this.colliding = c; }

    public void setColor(float r, float g, float b) {
        this.r = r; this.g = g; this.b = b;
    }
    public void setTexture(String path) {
        if (textureId != 0) glDeleteTextures(textureId);
        this.textureId = TextureUtils.loadTexture(path);
    }

    public void draw() {
        glEnable(GL_MULTISAMPLE);
        if (textureId != 0) {
            ShapeRenderer.drawTexturedCircle(
                (float)position.getX(),
                (float)position.getY(),
                (float)size,
                64,
                textureId
            );
        } else {
            glColor3f(r, g, b);
            ShapeRenderer.drawCircle(
                (float)position.getX(),
                (float)position.getY(),
                (float)size,
                64
            );
        }
        glDisable(GL_MULTISAMPLE);
    }

    public void dispose() {
        if (textureId != 0) {
            glDeleteTextures(textureId);
            textureId = 0;
        }
    }

    public static class Builder {
        double mass, size, x, y, vx = 0.0, vy = 0.0;
        boolean fixed = false;
        float r = 1.0f, g = 0.0f, b = 0.0f;
        String texturePath;

        public Builder mass(double m)          { mass = m; return this; }
        public Builder size(double s)          { size = s; return this; }
        public Builder position(double xx, double yy) { x = xx; y = yy; return this; }
        public Builder velocity(double vx_, double vy_){ vx = vx_; vy = vy_; return this; }
        public Builder fixed(boolean f)        { fixed = f; return this; }
        public Builder color(float rr, float gg, float bb){ r = rr; g = gg; b = bb; return this; }
        public Builder texture(String p)       { texturePath = p; return this; }

        public Body build() {
            if (mass <= 0 || size <= 0) throw new IllegalStateException("Mass/size must be positive");
            return new Body(this);
        }
    }
}