package com.ensea_java_final;

public class Vector2D {
    // Fields
    public Double x;
    public Double y;

    // Constructor
    public Vector2D(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    // Arithmetic operations
    public Vector2D add(Vector2D v) {
        return new Vector2D(x + v.x, y + v.y);
    }

    public Vector2D subtract(Vector2D v) {
        return new Vector2D(x - v.x, y - v.y);
    }

    public Vector2D scale(Double scalar) {
        return new Vector2D(x * scalar, y * scalar);
    }

    public Vector2D scale(Vector2D scalar) {
        return new Vector2D(x * scalar.x, y * scalar.y);
    }

    // Vector properties
    public Double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public Vector2D normalize() {
        Double mag = magnitude();
        return (mag == 0.0) ? new Vector2D(0.0, 0.0)
                            : new Vector2D(x / mag, y / mag);
    }

    public Double distance(Vector2D other) {
        Double dx = x - other.x;
        Double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Double dot(Vector2D other) {
        return x * other.x + y * other.y;
    }

    // Object overrides
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}