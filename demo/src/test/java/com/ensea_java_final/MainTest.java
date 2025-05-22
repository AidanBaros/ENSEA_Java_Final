package com.ensea_java_final;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.MockedStatic;
import static org.mockito.Mockito.*;


public class MainTest {
    // glfw = graphics library framework, (creates window, handles inputs and manages monitor)
    // gl is function loader binds openGL context to java runtime helper class for LWJGL that sets up openGL function pointers
    // gl immeidate java bindings need an active OpenGL so we stub them
    @Test
    void init_4by3Window_BoundSet() {
        // traditional mocked static can only stub/verify instances
        // we need mockedstatic bc the static methods are used through mockito and used in the scope of the behavior 
        // after behavior returns to normal
        // mocking window, glfw Opengl bindings, and gl11 methods
        try(MockedStatic<WindowManager> wm = mockStatic(WindowManager.class); // 
            MockedStatic<org.lwjgl.glfw.GLFW> glfw = mockStatic(org.lwjgl.glfw.GLFW.class); // stops native window creating 
            MockedStatic<org.lwjgl.opengl.GL> gl = mockStatic(org.lwjgl.opengl.GL.class); // avoid real open gl functions
            MockedStatic<org.lwjgl.opengl.GL11> gl11 = mockStatic(org.lwjgl.opengl.GL11.class)) // stops exec of native OpenGL  that needs real OpenGL

            {

                // inv -> null lambda expression ignore what was passed and do nothing
                wm.when(() -> WindowManager.init(anyInt(), anyInt(), any())).thenAnswer(inv -> null); // initialzie window
                wm.when(WindowManager::getWindow).thenReturn(0L); // this is our window
                wm.when(WindowManager::update).thenAnswer(inv -> null); // checks if update does nothing

                glfw.when(() -> org.lwjgl.glfw.GLFW.glfwWindowShouldClose(0L)).thenReturn(false).thenReturn(true);
                // runs the render once and ends loop


                // prevent native openGL initializations/calls
                gl.when(org.lwjgl.opengl.GL::createCapabilities).thenAnswer(inv -> null); // load gl bindings
                gl11.when(() -> org.lwjgl.opengl.GL11.glClear(anyInt())).thenAnswer(inv -> null); //  // mocks the OpenGL bufferclearing 
                // just use it for running tests
                // 
                Main.main(new String[0]); // run the main

                wm.verify(() -> WindowManager.init(800, 600, "Physics Simulation")); // verify ran
                wm.verify(WindowManager::cleanup);

            }

    }
}
