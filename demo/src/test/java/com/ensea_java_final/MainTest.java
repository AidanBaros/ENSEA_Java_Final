package com.ensea_java_final;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.MockedStatic;
import static org.mockito.Mockito.*;


public class MainTest {
    
    @Test
    void init_4by3Window_BoundSet() {
        try(MockedStatic<WindowManager> wm = mockStatic(WindowManager.class);
            MockedStatic<org.lwjgl.glfw.GLFW> glfw = mockStatic(org.lwjgl.glfw.GLFW.class);
            MockedStatic<org.lwjgl.opengl.GL> gl = mockStatic(org.lwjgl.opengl.GL.class);
            MockedStatic<org.lwjgl.opengl.GL11> gl11 = mockStatic(org.lwjgl.opengl.GL11.class))

            {

                // inv -> null lambda expression ignore what was passed and do nothing
                wm.when(() -> WindowManager.init(anyInt(), anyInt(), any())).thenAnswer(inv -> null);
                wm.when(WindowManager::getWindow).thenReturn(0L); // this is our window
                wm.when(WindowManager::update).thenAnswer(inv -> null);

                glfw.when(() -> org.lwjgl.glfw.GLFW.glfwWindowShouldClose(0L)).thenReturn(false).thenReturn(true);


                // prevent native openGL initializations/calls
                gl.when(org.lwjgl.opengl.GL::createCapabilities).thenAnswer(inv -> null);
                gl11.when(() -> org.lwjgl.opengl.GL11.glClear(anyInt())).thenAnswer(inv -> null);
                // 
                Main.main(new String[0]);

                wm.verify(() -> WindowManager.init(800, 600, "Physics Simulation"));
                wm.verify(WindowManager::cleanup);

            }

    }
}
