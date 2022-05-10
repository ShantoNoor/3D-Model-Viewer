package com.modelviewer.Window;

import com.modelviewer.Utils.Constants;
import com.modelviewer.Utils.Utils;
import com.modelviewer.Window.Input.KeyListener;
import com.modelviewer.Window.Input.MouseListener;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

abstract public class Window {
    private String title;
    protected int width, height;
    protected long glfwWindow;

    private boolean showFps;
    private Vector4f clearColor;
    private int windowFlags;

    private float fieldOfView;
    private float farPlane;
    private float nearPlane;
    protected Matrix4f projectionMatrix;

    public Window(int width, int height, String title) {
        this.height = height;
        this.width = width;

        this.title = title;
        this.showFps = true;

        this.clearColor = new Vector4f(0.15f, 0.15f, 0.15f, 1.0f);
        this.windowFlags = GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT;

        this.fieldOfView = 45;
        this.nearPlane = 0.1f;
        this.farPlane = 100.0f;

        updateProjectionMatrix();
    }


    private void updateProjectionMatrix() {
        float aspectRatio = (float) width / (float) height;
        projectionMatrix = new Matrix4f().perspective(fieldOfView * Constants.TO_RADIAN, aspectRatio, nearPlane, farPlane);
    }

    public void init() {
        System.out.println("LWJGL Version: " + Version.getVersion());

        // setup error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // init glfw
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to init GLFW.");
        }

        // config glfw
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        //glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        // create window
        glfwWindow = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
        if (glfwWindow == NULL) {
            throw new IllegalStateException("Failed to create GLFW window.");
        }

        glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);
        glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);

        // make the context opengl
        glfwMakeContextCurrent(glfwWindow);

        // enable v-sync
        glfwSwapInterval(1);

        // make window visible
        glfwShowWindow(glfwWindow);

        // important line
        GL.createCapabilities();

        //Enabling Clock Wise Face Culling
        glEnable(GL_CULL_FACE);
        glCullFace(GL_CW);

        // Enabling Depth Test
        glEnable(GL_DEPTH_TEST);

        System.out.println("OpenGL Version: " + glGetString(GL_VERSION));
    }

    public void run() {
        init();
        setup();
        mainLoop();
        clear();
    }

    public void mainLoop() {
        float beginTime = 0.0f;
        float endTime = 0.0f;
        float dt = 0.0f;

        while (!glfwWindowShouldClose(glfwWindow)) {
            beginTime = Utils.getTime();

            // Poll events
            glfwPollEvents();
            handleResize();


            glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
            glClear(windowFlags);

            loop(dt);

            glfwSwapBuffers(glfwWindow);

            endTime = Utils.getTime();
            dt = endTime - beginTime;

            if(showFps)
                glfwSetWindowTitle(glfwWindow, title + " FPS: " + (int) (1 / dt));
        }
    }

    public void clear() {
        // Free memory
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    abstract public void loop(float dt);
    abstract public void setup();

    private void handleResize() {
        IntBuffer windowWidth = BufferUtils.createIntBuffer(1);
        IntBuffer windowHeight = BufferUtils.createIntBuffer(1);
        glfwGetWindowSize(glfwWindow, windowWidth, windowHeight);
        width = windowWidth.get();
        height = windowHeight.get();
        updateProjectionMatrix();
    }

    public String getTitle() {
        return title;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getGlfwWindow() {
        return glfwWindow;
    }

    public float getFieldOfView() {
        return fieldOfView;
    }

    public float getFarPlane() {
        return farPlane;
    }

    public float getNearPlane() {
        return nearPlane;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setWidth(int width) {
        this.width = width;
        updateProjectionMatrix();
    }

    public void setHeight(int height) {
        this.height = height;
        updateProjectionMatrix();
    }

    public void setShowFps(boolean showFps) {
        this.showFps = showFps;
    }

    public void setFieldOfView(float fieldOfView) {
        this.fieldOfView = fieldOfView;
        updateProjectionMatrix();
    }

    public void setFarPlane(float farPlane) {
        this.farPlane = farPlane;
        updateProjectionMatrix();
    }

    public void setNearPlane(float nearPlane) {
        this.nearPlane = nearPlane;
        updateProjectionMatrix();
    }

    public void setClearColor(Vector4f clearColor) {
        this.clearColor = clearColor;
    }
}