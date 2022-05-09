package com.modelviewer.Tests.NuklearDemo2;

import com.modelviewer.Camera.Camera;
import com.modelviewer.Renderer.Mesh.Model;
import com.modelviewer.Renderer.Shader.ShaderProgram;
import com.modelviewer.Renderer.Texture;
import com.modelviewer.Utils.Constants;
import com.modelviewer.Window.Window;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Bag extends Window {
    private Camera camera;
    private ShaderProgram shaderProgram;
    private Model model;
    private Texture baseColor;
    private Texture normalMap;
    private Texture aoMap;
    private Texture metalnessMap;
    private Texture roughnessMap;

    public Bag(int width, int height, String title) {
        super(width, height, title);
    }

    Matrix4f rotate;

    @Override
    public void setup() {
        shaderProgram = new ShaderProgram("shaders/default.vs.glsl", "shaders/default.fs.glsl");
        shaderProgram.compile();
        shaderProgram.safeUpload("flipTexCordY", 1);

        normalMap = new Texture();
        normalMap.init("TestModels/backpack/normal.png");

        baseColor = new Texture();
        baseColor.init("TestModels/backpack/diffuse.jpg");

        aoMap = new Texture();
        aoMap.init("TestModels/backpack/ao.jpg", true);

        metalnessMap = new Texture();
        metalnessMap.init("TestModels/backpack/specular.jpg", true);

        roughnessMap = new Texture();
        roughnessMap.init("TestModels/backpack/roughness.jpg", true);

        model = new Model();
//        model.loadMesh("TestModels/dragon.obj");
//        model.loadMesh("TestModels/dragon/Dragon_Baked_Actions_fbx_7.4_binary.fbx");
//        model.loadMesh("TestModels/nanosuit/nanosuit.obj");
        model.loadMesh("TestModels/backpack/backpack.obj");
//        model.loadMesh("TestModels/rifel/source/rifel.obj");
//        model.loadMesh("TestModels/car/source/porsche not subd.fbx");
//        model.loadMesh("TestModels/pistol2/source/pistol.fbx");
//        model.loadMesh("TestModels/pistol/source/pistol.fbx");
//        model.loadMesh("TestModels/simple-sword/source/sword.glb");
//        model.loadMesh("TestModels/house.dae");
//        model.loadMesh("TestModels/Drawing1.stl");
//        model.loadMesh("TestModels/Drawing8.stl");
//        model.loadMesh("TestModels/Drawing5.stl");
//        model.loadMesh("TestModels/Drawing4.stl");
//        model.loadMesh("TestModels/Drawing7.stl");
//        model.loadMesh("TestModels/cube.obj");
//        model.loadMesh("TestModels/plane.stl");
//        model.loadMesh("TestModels/astronaut/source/Walking astronaut.glb");

        camera = new Camera();
        camera.setPosition(new Vector3f(0.0f, 0.0f, model.getDistance()));

        rotate = new Matrix4f();
    }

    @Override
    public void loop(float dt) {
        camera.mouseUpdate(glfwWindow, width, height);
        camera.keyboardUpdate(dt);

        shaderProgram.safeUpload("projection", projectionMatrix);

        shaderProgram.safeUpload("view", camera.getViewMatrix());
        shaderProgram.safeUpload("camPos", camera.getPosition());
        shaderProgram.safeUpload("lightF", new Vector3f(100, 100.0f, 100.0f));
        shaderProgram.safeUpload("rotate", rotate.rotate(dt, Constants.Y_AXIS));


        baseColor.bind(shaderProgram, "baseColor", 0);
        normalMap.bind(shaderProgram, "normalMap", 1);
        aoMap.bind(shaderProgram, "aoMap", 2);
        metalnessMap.bind(shaderProgram, "metalnessMap", 3);
        roughnessMap.bind(shaderProgram, "roughnessMap", 4);

        model.render(shaderProgram);
    }

    public static void main(String[] args) {
        new Bag(1200, 800, "Bag Model").run();
    }
}
