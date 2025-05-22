package com.ensea_java_final;

// taken from lwjgl website 
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.lwjgl.system.MemoryUtil.memAddress;

/*
 * math check:
 * given fixed window size we 
 */

// GLFW this is window creation and monitoring
// gl and GL11 they are openGL context and rendering functions
// verify bounds are correct 
@DisplayName("WindowManager projection maths")
public class WindowManagerTest {

    @Test
    void init_4by3Window_BoundSet() {

        // MockedStatic<WindowManager> wm = mockStatic(WindowManager.class,
        // CALLS_REAL_METHODS);
        /// call window without opening a real window
        try (MockedStatic<org.lwjgl.glfw.GLFW> glfw = mockStatic(org.lwjgl.glfw.GLFW.class);
                MockedStatic<org.lwjgl.opengl.GL> gl = mockStatic(org.lwjgl.opengl.GL.class)) {
            // simulating initialization
            glfw.when(org.lwjgl.glfw.GLFW::glfwInit).thenReturn(true);
            // this fake successufl glfw opening

            // this is simulating creating a window and returning a fake window
            glfw.when(() -> org.lwjgl.glfw.GLFW.glfwCreateWindow(anyInt(), anyInt(), any(CharSequence.class), anyLong(),
                    anyLong())).thenReturn(7L);

            glfw.when(org.lwjgl.glfw.GLFW::glfwGetPrimaryMonitor).thenReturn(1L);
            // when we call getPrimaryMonitor by GLFW just return 1l(monitor id)

            // we are simulating a monitor resolution of 1920 by 1080
            // GLFWVidMode mode = mock(GLFWVidMode.class);
            // when(mode.width()).thenReturn(1920);
            // when(mode.height()).thenReturn(1080);
            // // wm.when(WindowManager::cleanup).thenAnswer(inv -> null);
            // glfw.when(() -> org.lwjgl.glfw.GLFW.glfwGetVideoMode(1L)).thenReturn(mode);

            // return a real struct, no mocking

            // GLFWVidMode mode = GLFWVidMode.create();
            // building a struct for GLFWVidMode

            // mode.width(1920).height(1080);

            // GLFW is viewing real memory but mockito cannot mock native/real memory logic
            // so we create memory
            ByteBuffer buf = BufferUtils.createByteBuffer(GLFWVidMode.SIZEOF);
            buf.putInt(0, 1920); // width
            buf.putInt(4, 1080); // height
            buf.putInt(8, 8);  // red
            buf.putInt(12, 8); // green 
            buf.putInt(16, 8); // blue
            buf.putInt(20, 60); // refresh/frame rate  taken from struct
            GLFWVidMode mode = GLFWVidMode.create(memAddress(buf));

            glfw.when(() -> org.lwjgl.glfw.GLFW.glfwGetVideoMode(1L)).thenReturn(mode);

            gl.when(org.lwjgl.opengl.GL::createCapabilities).thenAnswer(inv -> null);

            // we are calling init() with a 400 x 300 window (aspect = 1.333)
            WindowManager.init(400, 300, "test");

            // check the computed projection bounds are correct
            float aspect = 400f / 300f;

            // this checks the aspect ratio (width / height
            // checks if -1.33 is the left bound), 1.33 is right bound, -1 bottom bound, 1
            // top bound
            assertAll(
                    // we checking if horizontal view is symmetric when window wide a
                    // and if vertical view is constant
                    () -> assertEquals(-aspect, WindowManager.LEFT_BOUND, 1e-6),
                    () -> assertEquals(aspect, WindowManager.RIGHT_BOUND, 1e-6),
                    () -> assertEquals(-1.0f, WindowManager.BOTTOM_BOUND, 1e-6),
                    () -> assertEquals(1.0f, WindowManager.TOP_BOUND, 1e-6));
        }
    }
}
