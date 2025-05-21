package com.ensea_java_final;

public class Vector2D {
    public Double x, y;

    public Vector2D(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D add(Vector2D v) {
        return new Vector2D(this.x + v.x, this.y + v.y);
    }

    public Vector2D subtract(Vector2D v) {
        return new Vector2D(this.x - v.x, this.y - v.y);
    }

    public Vector2D scale(Double scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }

    public Vector2D scale(Vector2D scalar) {
        return new Vector2D(this.x * scalar.x, this.y * scalar.y);
    }

    public Double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public Vector2D normalize() {
        Double mag = magnitude();
        if (mag == 0) return new Vector2D(0.0, 0.0);
        return new Vector2D(x / mag, y / mag);
    }

    public Double distance(Vector2D other) {
        Double dx = this.x - other.x;
        Double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Double dot(Vector2D other) {
        return this.x * other.x + this.y * other.y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}