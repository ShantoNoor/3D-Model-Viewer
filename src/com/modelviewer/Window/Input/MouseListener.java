package com.modelviewer.Window.Input;

import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class MouseListener {
    private static MouseListener instance;
    private double xPos, yPos, lastX, lastY;
    private double scrollX, scrollY;
    private boolean mouseButtonPressed[] = new boolean[3];
    private boolean isDragging, isScrolling;
    public boolean insideMainWindow;

    private MouseListener() {
        this.scrollX = 0.0;
        this.scrollY = 0.0;
        this.xPos = 0.0;
        this.yPos = 0.0;
        this.lastX = 0.0;
        this.lastY = 0.0;
    }

    public static MouseListener get() {
        if(instance == null) {
            instance = new MouseListener();
        }
        return instance;
    }

    public static void mousePosCallback(long window, double xpos, double ypos) {
        get().lastX = get().xPos;
        get().lastY = get().yPos;
        get().xPos = xpos;
        get().yPos = ypos;

        get().isDragging = get().mouseButtonPressed[0] | get().mouseButtonPressed[1] | get().mouseButtonPressed[2];
    }

    public static void mouseButtonCallback(long window, int button, int action, int mods)  {
        if(action == GLFW_PRESS) {
            if(button < get().mouseButtonPressed.length) {
                get().mouseButtonPressed[button] = true;
            }
        } else if(action == GLFW_RELEASE) {
            if(button < get().mouseButtonPressed.length) {
                get().mouseButtonPressed[button] = false;
                get().isDragging = false;
            }
        }
    }

    public static void mouseScrollCallback(long window, double xOffset, double yOffset) {
        get().scrollX = xOffset;
        get().scrollY = yOffset;
        get().isScrolling = true;
    }

    public static float getX() {
        return (float) get().xPos;
    }

    public static float getY() {
        return (float) get().yPos;
    }
    
    public static Vector2f getXY() {
        return new Vector2f(getX(), getY());
    }

    public static float getDx() {
        float dx = (float) ( get().lastX - get().xPos);
        get().lastX = get().xPos;
        return dx;
    }

    public static float getDy() {
        float dy = (float) ( get().lastY - get().yPos);
        get().lastY = get().yPos;
        return dy;
    }

    public static Vector2f getDxy() {
        return new Vector2f(getDx(), getDy());
    }

    public static boolean isDragging() {
        return get().isDragging & get().insideMainWindow;
    }

    public static boolean isScrolling() {
        return get().isScrolling & get().insideMainWindow;
    }

    public static void stopScrolling() {
        get().isScrolling = false;
    }

    public static boolean isMouseButtonPressed(int button) {
        if(button < get().mouseButtonPressed.length) {
            return get().mouseButtonPressed[button];
        }
        return false;
    }

    public static float getScrollX() {
        return (float) get().scrollX;
    }

    public static float getScrollY() {
        return (float) get().scrollY;
    }

    public static Vector2f getScrollXY() {
        return new Vector2f(getScrollX(), getScrollY());
    }
}