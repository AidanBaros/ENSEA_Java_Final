package com.ensea_java_final;

public class Vector2D {
    private double x, y;
    public Vector2D(double x, double y) { this.x = x; this.y = y; }
    public double getX()                { return x; }
    public double getY()                { return y; }
    public Vector2D add(Vector2D o)     { return new Vector2D(x + o.x, y + o.y); }
    public Vector2D subtract(Vector2D o){ return new Vector2D(x - o.x, y - o.y); }
    public Vector2D scale(double s)     { return new Vector2D(x * s, y * s); }
    public double magnitude()           { return Math.hypot(x, y); }
    public double magnitudeSquared()    { return x * x + y * y; }
    public Vector2D normalize() {
        double m = magnitude();
        return (m > 0) ? scale(1.0 / m) : new Vector2D(0, 0);
    }
    public double dot(Vector2D o)       { return x * o.x + y * o.y; }
}