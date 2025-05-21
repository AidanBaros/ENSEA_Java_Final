package com.ensea_java_final;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


// import static org.lwjgl.opengl.GL11.*;



@DisplayName("Body builder, mutators and drawing logic")
public class BodyTest {

    // test the body building, mutators and the drawing logic

    
    // fields are created correctly
    @Test
    @DisplayName("Builder populuates the fields correct")
    void builder_ok() {
        Body b = new Body.Builder().mass(2.0)
        .size(0.5).position(1.0, -1.0).velocity(0.1, 0.2)
        .build();

        assertAll(
            // lambda 
            () -> assertEquals(2.0, b.getMass()),
            () -> assertEquals(0.5, b.getSize()),
            () -> assertEquals(1.0, b.getPosition().x),
            () -> assertEquals(-1.0, b.getPosition().y),
            () -> assertEquals(0.1, b.getVelocity().x),
            () -> assertEquals(0.2, b.getVelocity().y)
            );

    }


    // the builder is enforcing the mandatory fields
    @Test
    @DisplayName("Builder enforces mand fields")
    void builderMissingMandFields() {
        // we are missing the size and position
        Body.Builder builder = new Body.Builder().mass(1.0);
        assertThrows(IllegalStateException.class, () -> builder.build());
    }

        // test setters change the state/mutate
    @Test
    @DisplayName("Setters actually mutate states")
    void setters() {
        Body b = new Body.Builder().mass(1.0).size(0.1).position(0.0, 0.0).build();

        Vector2D newPosition = new Vector2D(9.0, 9.0);
        b.setPosition(newPosition);
        b.setMass(3.0);

        assertSame(newPosition, b.getPosition());
        assertEquals(3.0, b.getMass());

    }

    // test draw() ahs the expected GL calls
    @Test
    @DisplayName("draw() issues the expected GL calls")
    void drawCircleTest() {
        // mocks the static method to gl11 where it contains the openGL drawing functions
        // cannot use mock because mock is based to instances and mockStatic is based to static methods
        // gl11 has static methods which cannot be mocked so we use mockStatic to get those calls
        // mocks typically use with instances 
        // we are mocking static methods using gl11 class from LWJGL
        // gl is the context    
        // we create a mock that takes all static method calls to gl11 inside the block
        try (MockedStatic<Body> body = mockStatic(Body.class)) {
            Body planet = new Body.Builder().mass(1.0).size(0.5).position(0.2, 0.3).build();


            planet.draw();


            // cannot verify every vertex as it would be tedious and unnecessary
            // therefore we verify the circle started drawing and ended
            // gl.verify(() -> glBegin(GL_TRIANGLE_FAN));
            // gl.verify(() -> glEnd());

            body.verify(() -> 
            Body.drawCircle(eq(0.2f), eq(0.3f), 
            eq(0.5f), eq(32)
            ));
            body.verifyNoMoreInteractions();

        }
    }
}
