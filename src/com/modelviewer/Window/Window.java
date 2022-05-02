package com.modelviewer.Window;

import com.modelviewer.Camera.Camera;
import com.modelviewer.Renderer.Mesh.Model;
import com.modelviewer.Renderer.Shader;
import com.modelviewer.Renderer.Texture;
import com.modelviewer.Renderer.Quad;
import com.modelviewer.Renderer.VAO;
import com.modelviewer.Utils.Constants;
import com.modelviewer.Utils.Utils;
import com.modelviewer.Window.Input.KeyListener;
import com.modelviewer.Window.Input.MouseListener;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private int width, height;
    private String title;
    private long glfwWindow;
    private boolean showFps;
    private Vector4f clearColor;
    private int windowFlags;
    private Matrix4f projectionMatrix;

    private static Window window = null;

    private Window() {
        this.height = 720;
        this.width = 1280;
        this.title = "3D Model Viewer";
        this.showFps = true;
        this.clearColor = new Vector4f(0.15f, 0.15f, 0.15f, 1.0f);
        this.windowFlags = GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT;

        this.projectionMatrix = new Matrix4f().identity();
        projectionMatrix.perspective(45 * Constants.TO_RADIAN, ((float) width / (float) height), 0.1f, 100.0f, projectionMatrix);
    }

    public static Window get() {
        if (Window.window == null) {
            Window.window = new Window();
        }
        return Window.window;
    }

    public void run() {
        System.out.println("LWJGL Version: " + Version.getVersion());

        init();
        loop();

        // Free memory
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void init() {
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

    public void loop() {
        float beginTime = 0.0f;
        float endTime = 0.0f;
        float dt = 0.0f;

        Camera camera = new Camera();

        Shader shader = new Shader("shaders/default.vs.glsl", "shaders/default.fs.glsl");
        shader.compile();
        shader.upload("projection", projectionMatrix);

        Quad quad = new Quad();
        Shader quadShader = new Shader("shaders/quad.vs.glsl", "shaders/quad.fs.glsl");
        quadShader.compile();

        Model model = new Model();
        model.loadMesh("TestModels/pistol/source/pistol.fbx");

        Texture texture = new Texture();
        texture.init("TestModels/pistol2/textures/SAI_Glock19_albedo.tga.png");

        Texture normalMap = new Texture();
        normalMap.init("TestModels/pistol2/textures/SAI_Glock19_normal.tga.png");

        camera.setPosition(new Vector3f(0.0f, 0.0f, model.getDistance()));

        VAO vao = new VAO();

        while (!glfwWindowShouldClose(glfwWindow)) {
            beginTime = Utils.getTime();

            // Poll events
            glfwPollEvents();

            glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
            glClear(windowFlags);

            camera.mouseUpdate(glfwWindow, width, height);
            camera.keyboardUpdate(dt);

            shader.upload("view", camera.getViewMatrix());
            shader.upload("transform", model.getTransform());
            shader.upload("camPos", camera.getPosition());
            shader.upload("lightF", camera.getViewDirection());

            texture.bind(shader, "tex", 0);
            texture.bind(shader, "nor", 1);

//            vao.bind();
//            glLineWidth(4);
//            glBegin(GL_LINE);
//            glVertex3d(-1000, 0, 0);
//            glVertex3d(1000, 0, 0);
//            glEnd();
//            vao.unbind();

//            quad.render(quadShader);
            model.render(shader);

            glfwSwapBuffers(glfwWindow);

            endTime = Utils.getTime();
            dt = endTime - beginTime;

            if(showFps)
                glfwSetWindowTitle(glfwWindow, title + " FPS: " + (int) (1 / dt));
        }
    }
}
