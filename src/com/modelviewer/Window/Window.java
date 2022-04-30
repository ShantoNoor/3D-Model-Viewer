package com.modelviewer.Window;

import com.modelviewer.Camera.Camera;
import com.modelviewer.Renderer.Mesh.Model;
import com.modelviewer.Renderer.Shader;
import com.modelviewer.Renderer.Texture;
import com.modelviewer.Renderer.shapes.Quad;
import com.modelviewer.Utils.Color;
import com.modelviewer.Utils.Constants;
import com.modelviewer.Utils.Time;
import com.modelviewer.Utils.Utils;
import com.modelviewer.Window.IO.KeyListener;
import com.modelviewer.Window.IO.MouseListener;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.GL_MAX_TEXTURE_IMAGE_UNITS;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private int width, height;
    private String title;
    private long glfwWindow;
    private boolean showFps;
    private Color clearColor;
    private int windowFlags;
    private Matrix4f projectionMatrix;

    private static Window window = null;

    private Window() {
        this.height = 720;
        this.width = 1280;
        this.title = "3D Model Viewer";
        this.showFps = true;
        this.clearColor = new Color(0.15f, 0.15f, 0.15f, 1.0f);
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

        glEnable(GL_CULL_FACE);
        glCullFace(GL_CW);

        glEnable(GL_DEPTH_TEST);

        System.out.println("OpenGL Version: " + glGetString(GL_VERSION));

        IntBuffer maximumTextureUnits = BufferUtils.createIntBuffer(1);
        glGetIntegerv(GL_MAX_TEXTURE_IMAGE_UNITS, maximumTextureUnits);
        System.out.println(maximumTextureUnits.get());
    }

    public void loop() {
        float beginTime = 0.0f;
        float dt = 0.0f;
        float endTime = 0.0f;

        Camera camera = new Camera();
        Shader shader = new Shader("shaders/default.glsl");
        shader.upload("projection", projectionMatrix);

        Quad quad = new Quad();
        Shader quadShader = new Shader("shaders/quad.glsl");
        quadShader.upload("projection", projectionMatrix);

        Model model = new Model();
//        model.loadMesh("TestModels/dragon.obj");
//        model.loadMesh("TestModels/backpack/backpack.obj");
//        model.loadMesh("TestModels/rifel/source/rifel.obj");
//        model.loadMesh("TestModels/pistol2/source/pistol.fbx");
//        model.loadMesh("TestModels/pistol/source/pistol.fbx");
//        model.loadMesh("TestModels/simple-sword/source/sword.glb");
//        model.loadMesh("TestModels/house.dae");
//        model.loadMesh("TestModels/Drawing1.stl");
//        model.loadMesh("TestModels/Drawing8.stl");
//        model.loadMesh("TestModels/Drawing5.stl");
        model.loadMesh("TestModels/Drawing4.stl");
//        model.loadMesh("TestModels/Drawing7.stl");
//        model.loadMesh("TestModels/cube.obj");
//        model.loadMesh("TestModels/plane.stl");

        Matrix4f transform = new Matrix4f();

        float diameter = Vector3f.distance(model.max.x, model.max.y, model.max.z, model.min.x, model.min.y, model.min.z);

        float diameterLimitMin = 15.0f;
        float diameterLimitMax = 30.0f;
        if(diameter > diameterLimitMax || diameter < diameterLimitMin) {

            if(diameter > diameterLimitMax) {
                new Matrix4f().scale(diameterLimitMax / diameter).mul(transform, transform);
            } else {
                new Matrix4f().scale(diameterLimitMin / diameter).mul(transform, transform);
            }

            model.max = Utils.mulVector3fWithMatrix4f(model.max, transform);
            model.min = Utils.mulVector3fWithMatrix4f(model.min, transform);

            diameter = Vector3f.distance(model.max.x, model.max.y, model.max.z, model.min.x, model.min.y, model.min.z);
        }

        Vector3f modelOrigin = model.max.add(model.min, new Vector3f()).mul(0.5f);
        Utils.printVector3f(modelOrigin, "modelO");

        Vector3f modelTranlation = new Vector3f();
        modelOrigin.mul(-1.0f, modelTranlation);

        Matrix4f modelTransform = new Matrix4f().translate(modelTranlation);
        modelTransform.mul(transform, transform);
        modelOrigin = Utils.mulVector3fWithMatrix4f(modelOrigin, modelTransform);

        Vector3f axisToRotate = new Vector3f(1.0f, 0.0f, 0.0f);

        Utils.printVector3f(modelOrigin, "modelO");

        camera.setPosition(new Vector3f(0.0f, 0.0f, diameter + 5));

        Matrix4f quadTransform = new Matrix4f();
        new Matrix4f().scale(100).mul(quadTransform, quadTransform);
        new Matrix4f().rotate(90*Constants.TO_RADIAN, axisToRotate).mul(quadTransform, quadTransform);
        new Matrix4f().translate(0.0f, -model.max.y, 0.0f).mul(quadTransform, quadTransform);

        Texture texture = new Texture();
        texture.init("TestModels/pistol2/textures/SAI_Glock19_albedo.tga.png");
//        texture.init("TestModels/backpack/diffuse.jpg");
//        texture.init("TestModels/rifel/textures/HK443_low_HK443_Lowpoly_BaseColor.png");

        while (!glfwWindowShouldClose(glfwWindow)) {
            beginTime = Time.getTime();

            // Poll events
            glfwPollEvents();

            glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
            glClear(windowFlags);

            camera.mouseUpdate(glfwWindow, width, height);
            camera.keyboardUpdate(dt);

//            Matrix4f rotation = new Matrix4f();
//            new Matrix4f().translate(modelOrigin.mul(-1.0f, new Vector3f())).mul(rotation, rotation);
//            new Matrix4f().rotate(dt, axisToRotate.normalize()).mul(rotation, rotation);
//            new Matrix4f().translate(modelOrigin).mul(rotation, rotation);
//            rotation.mul(transform, transform);

            shader.upload("view", camera.getViewMatrix());
            shader.upload("transform", transform);
            shader.upload("camPos", camera.getPosition());
            shader.upload("lightF", camera.getViewDirection());

            shader.upload("tex", 0);
            glActiveTexture(GL_TEXTURE0);
            texture.bind();

            quadShader.upload("view", camera.getViewMatrix());
            quadShader.upload("transform", quadTransform);

            quad.render(quadShader);
            model.render(shader);

            glfwSwapBuffers(glfwWindow);

            endTime = Time.getTime();
            dt = endTime - beginTime;

            if(showFps)
                glfwSetWindowTitle(glfwWindow, title + " FPS: " + (int) (1 / dt));

            if(KeyListener.isKeyPressed(GLFW_KEY_X)) axisToRotate = new Vector3f(1.0f, 0.0f, 0.0f);
            if(KeyListener.isKeyPressed(GLFW_KEY_Y)) axisToRotate = new Vector3f(0.0f, 1.0f, 0.0f);
            if(KeyListener.isKeyPressed(GLFW_KEY_Z)) axisToRotate = new Vector3f(0.0f, 0.0f, 1.0f);
            if(KeyListener.isKeyPressed(GLFW_KEY_C)) axisToRotate = new Vector3f(0.0f, 1.0f, 1.0f);
        }
    }
}
