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

    public Vector2D scale(double scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public Vector2D normalize() {
        double mag = magnitude();
        if (mag == 0) return new Vector2D(0.0, 0.0);
        return new Vector2D(x / mag, y / mag);
    }

    
}