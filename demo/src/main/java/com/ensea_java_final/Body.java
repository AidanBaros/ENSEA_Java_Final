package com.ensea_java_final;

import static org.lwjgl.opengl.GL11.*;

public class Body {
    private Double mass, size;
    private Vector2D position;
    private Vector2D velocity;
    private Vector2D acceleration;
    private Boolean fixed;
    private Boolean isColliding;

    // Private constructor: enforce use of Builder
    private Body(Builder builder) {
        this.mass = builder.mass;
        this.size = builder.size;
        this.position = new Vector2D(builder.x, builder.y);
        this.velocity = new Vector2D(builder.vx, builder.vy);
        this.acceleration = new Vector2D(0.0, 0.0);
        this.fixed = builder.fixed;
    }

    // --- Getters ---
    public Double getMass() { return mass; }
    public Double getSize() { return size; }
    public Vector2D getPosition() { return position; }
    public Vector2D getVelocity() { return velocity; }
    public Vector2D getAcceleration() { return acceleration; }
    public Boolean isFixed() { return fixed; }
    public Boolean isColliding() {return isColliding; }

    // --- Setters ---
    public void setMass(Double mass) {this.mass = mass;}
    public void setSize(Double size) {this.size = size;}
    public void setPosition(Vector2D position) {this.position = position;}
    public void setVelocity(Vector2D velocity) {this.velocity = velocity;}
    public void setAcceleration(Vector2D acceleration) {this.acceleration = acceleration;}
    public void setFixed(Boolean fixed) {this.fixed = fixed;}
    public void setColliding(Boolean colliding) {this.isColliding = colliding; }


    // --- Functions ---
    public void move(Vector2D position){
        if (!fixed){
            this.position = position;
        }
        else{
            throw new IllegalStateException("Body is fixed and can not be moved");
        }
    }

    public void draw() {
        drawCircle(this.position.x.floatValue(), this.position.y.floatValue(), this.size.floatValue(), 32);
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


    // --- Builder ---
    public static class Builder {
        private Double mass, size;
        private Double x, y;
        private Double vx = 0.0, vy = 0.0; // default to 0 if not set
        private Boolean fixed = false;
        private Boolean isColliding = false;

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

        public Builder fixed(Boolean fixed) {
            this.fixed = fixed;
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
