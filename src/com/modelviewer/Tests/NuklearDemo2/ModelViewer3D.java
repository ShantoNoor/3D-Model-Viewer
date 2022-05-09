package com.modelviewer.Tests.NuklearDemo2;

import com.modelviewer.Camera.Camera;
import com.modelviewer.Renderer.Mesh.Model;
import com.modelviewer.Renderer.Quad;
import com.modelviewer.Renderer.Shader.ShaderProgram;
import com.modelviewer.Renderer.Texture;
import com.modelviewer.Window.Window;
import org.joml.Vector3f;

public class ModelViewer3D extends Window {
    private Camera camera;
    private ShaderProgram shaderProgram;
    private Quad quad;
    private ShaderProgram quadShaderProgram;
    private Model model;
    private Texture texture;
    private Texture normalMap;

    public ModelViewer3D(int width, int height, String title) {
        super(width, height, title);
    }

    @Override
    public void setup() {
        shaderProgram = new ShaderProgram("shaders/default.vs.glsl", "shaders/default.fs.glsl");
        shaderProgram.compile();

        normalMap = new Texture();
        normalMap.init("TestModels/pistol2/textures/SAI_Glock19_normal.tga.png");
        texture = new Texture();
        texture.init("TestModels/pistol2/textures/SAI_Glock19_albedo.tga.png");

        model = new Model();
        model.loadMesh("TestModels/pistol/source/pistol.fbx");

        quad = new Quad();
        quadShaderProgram = new ShaderProgram("shaders/quad.vs.glsl", "shaders/quad.fs.glsl");
        quadShaderProgram.compile();

        camera = new Camera();
        camera.setPosition(new Vector3f(0.0f, 0.0f, model.getDistance()));
    }

    @Override
    public void loop(float dt) {
        camera.mouseUpdate(glfwWindow, width, height);
        camera.keyboardUpdate(dt);

        shaderProgram.safeUpload("projection", projectionMatrix);
        quadShaderProgram.safeUpload("projection", getProjectionMatrix());

        shaderProgram.safeUpload("view", camera.getViewMatrix());
        shaderProgram.safeUpload("camPos", camera.getPosition());
        shaderProgram.safeUpload("lightF", camera.getViewDirection());
        texture.bind(shaderProgram, "tex", 0);
        texture.bind(shaderProgram, "nor", 1);
        model.render(shaderProgram);

        quadShaderProgram.safeUpload("view", camera.getViewMatrix());
        quad.render(quadShaderProgram);
    }

    public static void main(String[] args) {
        new ModelViewer3D(1280, 720, "3D Model Viewer").run();
    }
}
