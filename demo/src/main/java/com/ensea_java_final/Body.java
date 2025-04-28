package com.ensea_java_final;

public class Body {
    private Double mass, size;
    private Vector2D position;
    private Vector2D velocity;
    private Vector2D force;

    // Private constructor: enforce use of Builder
    private Body(Builder builder) {
        this.mass = builder.mass;
        this.size = builder.size;
        this.position = new Vector2D(builder.x, builder.y);
        this.velocity = new Vector2D(builder.vx, builder.vy);
        this.force = new Vector2D(0.0, 0.0);
    }

// --- Getters ---
public Double getMass() { return mass; }
public Double getSize() { return size; }
public Vector2D getPosition() { return position; }
public Vector2D getVelocity() { return velocity; }

// --- Setters ---
public void setMass(Double mass) {this.mass = mass;}
public void setSize(Double size) {this.size = size;}
public void setPosition(Double x, Double y) {this.position.x = x; this.position.y = y;}
public void setVelocity(Double vx, Double vy) {this.velocity.x = vx; this.velocity.y = vy;}
public void setForce(Double fx, Double fy) {this.force.x = fx; this.force.y = fy;}

// --- Builder ---
public static class Builder {
    private Double mass, size;
    private Double x, y;
    private Double vx = 0.0, vy = 0.0; // default to 0 if not set

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

    public Body build() {
        if (mass == null || size == null || x == null || y == null) {
            throw new IllegalStateException("Mass, Size, and Position must be set.");
        }
        return new Body(this);
    }
}
}