package com.modelviewer.Tests;

import com.modelviewer.Camera.Camera;
import com.modelviewer.Renderer.Mesh.Model;
import com.modelviewer.Renderer.Shader.ShaderProgram;
import com.modelviewer.Renderer.Texture;
import com.modelviewer.Window.Window;
import org.joml.Vector3f;

public class MeshTester extends Window {
    private Camera camera;
    private ShaderProgram shaderProgram;
    private Model model;
    private Texture texture;
    private Texture normalMap;

    public MeshTester(int width, int height, String title) {
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
//        model.loadMesh("TestModels/dragon.obj");
//        model.loadMesh("TestModels/backpack/backpack.obj");
//        model.loadMesh("TestModels/rifel/source/rifel.obj");
//        model.loadMesh("TestModels/pistol2/source/pistol.fbx");
//        model.loadMesh("TestModels/pistol/source/pistol.fbx");
        model.loadMesh("TestModels/simple-sword/source/sword.glb");
//        model.loadMesh("TestModels/house.dae");
//        model.loadMesh("TestModels/Drawing1.stl");
//        model.loadMesh("TestModels/Drawing8.stl");
//        model.loadMesh("TestModels/Drawing5.stl");
//        model.loadMesh("TestModels/Drawing4.stl");
//        model.loadMesh("TestModels/Drawing7.stl");
//        model.loadMesh("TestModels/cube.obj");
//        model.loadMesh("TestModels/plane.stl");
//        model.loadMesh("/Users/shantonoor/IdeaProjects/3D Model Viewer/TestModels/astronaut/source/Walking astronaut.glb");

        camera = new Camera();
        camera.setPosition(new Vector3f(0.0f, 0.0f, model.getDistance()));
    }

    @Override
    public void loop(float dt) {
        camera.mouseUpdate(glfwWindow, width, height);
        camera.keyboardUpdate(dt);

        shaderProgram.safeUpload("projection", projectionMatrix);

        shaderProgram.safeUpload("view", camera.getViewMatrix());
        shaderProgram.safeUpload("transform", model.getTransform());
        shaderProgram.safeUpload("camPos", camera.getPosition());
        shaderProgram.safeUpload("lightF", camera.getViewDirection());
        texture.bind(shaderProgram, "tex", 0);
        texture.bind(shaderProgram, "nor", 1);
        model.render(shaderProgram);
    }

    public static void main(String[] args) {
        new MeshTester(800, 800, "Mesh Tester").run();
    }
}
