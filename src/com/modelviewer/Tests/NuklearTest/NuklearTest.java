package com.modelviewer.Tests.NuklearTest;

import com.modelviewer.Camera.Camera;
import com.modelviewer.Renderer.Mesh.Model;
import com.modelviewer.Renderer.Shader.ShaderProgram;
import com.modelviewer.Renderer.Texture;
import com.modelviewer.Utils.Constants;
import com.modelviewer.Window.Window;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class NuklearTest extends Window {
    private Camera camera;
    private ShaderProgram shaderProgram;
    private Model model;
    private Texture baseColor;
    private Texture normalMap;
    private Texture aoMap;
    private Texture metalnessMap;
    private Texture roughnessMap;

//    private final Calculator calc = new Calculator();

    public NuklearTest(int width, int height, String title) {
        super(width, height, title);
    }

    Matrix4f rotate;
    Demo demo;
    Calculator cal;

    @Override
    public void setup() {
        shaderProgram = new ShaderProgram("shaders/default.vs.glsl", "shaders/default.fs.glsl");
        shaderProgram.compile();

        normalMap = new Texture();
        normalMap.init("TestModels/pistol2/textures/SAI_Glock19_normal.tga.png");

        baseColor = new Texture();
        baseColor.init("TestModels/pistol2/textures/SAI_Glock19_albedo.tga.png");

        aoMap = new Texture();
        aoMap.init("TestModels/pistol2/textures/SAI_Glock19_AO.tga.png", true);

        metalnessMap = new Texture();
        metalnessMap.init("TestModels/pistol2/textures/SAI_Glock19_metalness.tga.png", true);

        roughnessMap = new Texture();
        roughnessMap.init("TestModels/pistol2/textures/SAI_Glock19_roughness.tga.png", true);

        model = new Model();

        model.loadMesh("TestModels/pistol2/source/pistol.fbx");
//        model.loadMesh("TestModels/pistol/source/pistol.fbx");

        camera = new Camera();
        camera.setPosition(new Vector3f(0.0f, 0.0f, model.getDistance()));

        rotate = new Matrix4f();

        demo = new Demo(ctx);
        cal = new Calculator(ctx);
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

        demo.layout(50, 50);
        cal.layout(300, 60);

        setClearColor(new Vector4f(demo.background.r(), demo.background.g(), demo.background.b(), demo.background.a()));
    }

    public static void main(String[] args) {
        new NuklearTest(1000, 600, "Nuklear Test").run();
    }
}
