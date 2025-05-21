package com.ensea_java_final;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.beans.Transient;


@DisplayName("Vector2D math")
public class Vector2DTest {

    @Test
    void add() {
        Vector2D v1 = new Vector2D(1.0, 2.0);
        Vector2D v2 = new Vector2D(3.0, 4.0);

        Vector2D r = v1.add(v2);

        assertEquals(4.0, r.x);
        assertEquals(6.0, r.y);
    }


    @Test
    void subtract() {
        Vector2D v1 = new Vector2D(5.0, 7.0);
        Vector2D v2 = new Vector2D(2.0, 1.0);

        Vector2D r = v1.subtract(v2);

        assertEquals(3.0, r.x);
        assertEquals(6.0, r.y);
    }


    @Test
    void scale() {
        Vector2D v = new Vector2D(-2.0, 3.0);
        
        Vector2D r = v.scale(4.0);

        assertEquals(-8.0, r.x);
        assertEquals(12.0, r.y);
    }


    @Test
    void magnitude() {
        // when using float point math, its not exact so we use tolerance 1e-9 for delta 
        // double check if correct tolerance
        assertEquals(5.0, new Vector2D(3.0, 4.0).magnitude(), 1e-8);
    }


    @Test
    void normalize_nonZero() {
        Vector2D unit = new Vector2D(10.0, 0.0).normalize();

        assertEquals(1.0, unit.magnitude(), 1e-9);
        assertEquals(1.0, unit.x);
        assertEquals(0.0, unit.y);
    }


    @Test
    void normalize_zeroVector() {
        Vector2D unit = new Vector2D(0.0, 0.0).normalize();
        
        assertEquals(0.0, unit.magnitude(), 1e-9);
    }


}
    
