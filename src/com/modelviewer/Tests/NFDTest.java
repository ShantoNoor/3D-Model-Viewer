package com.modelviewer.Tests;

import com.modelviewer.Camera.Camera;
import com.modelviewer.Renderer.Mesh.Material;
import com.modelviewer.Renderer.Mesh.Model;
import com.modelviewer.Renderer.Shader.ShaderProgram;
import com.modelviewer.Renderer.Texture;
import com.modelviewer.Utils.Constants;
import com.modelviewer.Utils.Utils;
import com.modelviewer.Window.NuklearLayer.*;
import com.modelviewer.Window.Window;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.util.tinyfd.TinyFileDialogs.*;

public class NFDTest extends Window {
    private Camera camera;
    private ShaderProgram shaderProgram;
    private Model model;

    public NFDTest(int width, int height, String title) {
        super(width, height, title);
    }

    private NuklearLayer ui;
    private SideBar sidebar;
    private NuklearColor cc;
    private NuklearImage baseColorMap;
    private NuklearImage normalMap;
    private NuklearImage aoMap;
    private NuklearImage metalnessMap;
    private NuklearImage roughnessMap;
    private Material mat;

    @Override
    public void setup() {
        shaderProgram = new ShaderProgram("shaders/default.vs.glsl", "shaders/default.fs.glsl");
        shaderProgram.compile();

        model = new Model();

        camera = new Camera();
        camera.setPosition(new Vector3f(0.0f, 0.0f, model.getDistance()));

        ui = new NuklearLayer(
                ctx,
                "NDF Test",
                Utils.createNkRect(30, 30, 230, 550),
                NK_WINDOW_BORDER | NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE | NK_WINDOW_TITLE | NK_WINDOW_SCALABLE
        );

        sidebar = new SideBar(ctx, 300, width, height);
        cc = new NuklearColor(ctx, "Clear Color", new Vector4f(0.0f), "");

        ApplyTheme.apply(ctx, NuklearLayerTheme.RED);

        Constants.nullTexture = new Texture();
        Constants.nullTexture.init("resources/null.jpg", false, false);

        baseColorMap = new NuklearImage(ctx, "Albedo Map", false, "baseColor", 0);
        aoMap = new NuklearImage(ctx, "Ambient Occlusion Map", true, "aoMap", 3);
        metalnessMap = new NuklearImage(ctx, "Metalness Map", true, "metalnessMap", 1);
        roughnessMap = new NuklearImage(ctx, "Roughness Map", true, "roughnessMap", 2);
        normalMap = new NuklearImage(ctx, "Normal Map", false, "normalMap", 4);

        mat = new Material(ctx);
    }

    NuklearFloat shineness = new NuklearFloat(ctx, "Shineness", 56.0f, 0.0f, 100.0f, 0.5f, 0.1f, "shineness");
    NuklearCheckbox use = new NuklearCheckbox(ctx, "Use");


    @Override
    public void loop(float dt) {

        camera.mouseUpdate(glfwWindow, width, height);
        camera.keyboardUpdate(dt);

        shaderProgram.safeUpload("projection", projectionMatrix);
        shaderProgram.safeUpload("view", camera.getViewMatrix());
        shaderProgram.safeUpload("camPos", camera.getPosition());
        shaderProgram.safeUpload("lightDir", new Vector3f(0, 110.0f, 110.0f));

        baseColorMap.safeUpload(shaderProgram);
        roughnessMap.safeUpload(shaderProgram);
        aoMap.safeUpload(shaderProgram);
        metalnessMap.safeUpload(shaderProgram);
        normalMap.safeUpload(shaderProgram);

        shineness.safeUpload(shaderProgram);

        model.updateRotation(dt);
        model.render(shaderProgram);

        try(MemoryStack stack = MemoryStack.stackPush()) {
            if(ui.begin()) {
                nk_layout_row_dynamic(ctx, 20, 2);
                if (nk_button_label(ctx, "Load")) {
                    CharSequence charSequence = new StringBuffer("jpg");
                    String modelPath = tinyfd_openFileDialog("Open a 3D Model File", "", null, "", false);
                    if(modelPath != null && model.loadMesh(modelPath))
                        camera.setPosition(new Vector3f(0.0f, 0.0f, model.getDistance()));
                }
                if (nk_button_label(ctx, "About")) {
                    info.show();
                    System.out.println("about");
                }
                sidebar.renderUi();
                shineness.updateAndRenderUi();
            }
            ui.end();

            if (sidebar.begin()) {
                model.renderUi(ctx);
                cc.updateAndRenderUi(stack);
                setClearColor(cc.get());

//                nk_layout_row_dynamic(ctx, 1838, 1);
//                if(nk_group_begin_titled(ctx, mat.materialName, mat.materialName, NK_WINDOW_BORDER | NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_TITLE)) {
////                    baseColorMap.updateAndRenderUi();
////                    metalnessMap.updateAndRenderUi();
////                    roughnessMap.updateAndRenderUi();
////                    aoMap.updateAndRenderUi();
////                    normalMap.updateAndRenderUi();
//                    nk_group_end(ctx);
//                }
                mat.updateAndRenderUi(stack);

            }
            sidebar.end();
        }
    }

    public static void main(String[] args) {
        new NFDTest(1000, 600, "NDF Test").run();
    }
}
