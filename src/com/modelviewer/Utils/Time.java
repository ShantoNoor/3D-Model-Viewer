package com.modelviewer.Utils;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Time {
    private static double timeStarted = glfwGetTime();
    public static float getTime() {
        return (float) (glfwGetTime() - timeStarted);
    }
}