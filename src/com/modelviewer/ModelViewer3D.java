package com.modelviewer;

import com.modelviewer.Camera.Camera;
import com.modelviewer.Renderer.CubeMap;
import com.modelviewer.Renderer.Mesh.Model;
import com.modelviewer.Renderer.Shader.ShaderProgram;
import com.modelviewer.Renderer.Texture;
import com.modelviewer.Utils.Constants;
import com.modelviewer.Utils.Utils;
import com.modelviewer.Window.NuklearLayer.*;
import com.modelviewer.Window.Window;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.util.tinyfd.TinyFileDialogs.*;

public class ModelViewer3D extends Window {
    private Camera camera;
    private ShaderProgram shaderProgram;
    private Model model;

    public ModelViewer3D(int width, int height, String title) {
        super(width, height, title);
    }

    private NuklearLayer ui;
    private NuklearSideBar sidebar;
    private NuklearColor cc;

    private ShaderProgram cubeMapShader;
    private CubeMap cubeMap;

    private int background = 1;

    @Override
    public void setup() {
        shaderProgram = new ShaderProgram("resources/shaders/default.vs.glsl", "resources/shaders/default.fs.glsl");
        shaderProgram.compile();

        model = new Model(ctx);

        camera = new Camera();
        camera.setPosition(new Vector3f(0.0f, 0.0f, model.getDistance()));

        ui = new NuklearLayer(
                ctx,
                "3D Model Viewer",
                Utils.createNkRect(5, 5, 230, 395),
                NK_WINDOW_BORDER | NK_WINDOW_MINIMIZABLE | NK_WINDOW_TITLE | NK_WINDOW_NO_SCROLLBAR
        );

        sidebar = new NuklearSideBar(ctx, 300, width, height);
        cc = new NuklearColor(ctx, "Background Color", new Vector4f(0.6f, 0.6f, 0.6f, 1.0f), "");

        NuklearTheme.apply(ctx, NuklearLayerTheme.RED);

        Constants.nullTexture = new Texture();
        Constants.nullTexture.init("resources/null.jpg", false, false);

        cubeMap = new CubeMap();
        cubeMap.addCubeMap("resources/CubeMaps/CubeMap1");
        cubeMap.addCubeMap("resources/CubeMaps/CubeMap2");
        cubeMapShader = new ShaderProgram("resources/shaders/cubeMap.vs.glsl", "resources/shaders/cubeMap.fs.glsl");
        cubeMapShader.compile();
    }

    @Override
    public void loop(float dt) {
        sidebar.updateSize(width, height);

        camera.mouseUpdate(glfwWindow, width, height);
        camera.keyboardUpdate(dt);

        shaderProgram.safeUpload("projection", projectionMatrix);
        shaderProgram.safeUpload("view", camera.getViewMatrix());
        shaderProgram.safeUpload("camPos", camera.getPosition());
        shaderProgram.safeUpload("lightDir", new Vector3f(0, 110.0f, 110.0f));
        cubeMap.getCubeMaps(0).safeBindCubeMap(shaderProgram, "env", 6);

        model.updateRotation(dt);
        model.render(shaderProgram);

        if(background > -1) {
            cubeMapShader.safeUpload("view", camera.getViewMatrix());
            cubeMapShader.safeUpload("projection", getProjectionMatrix());
            cubeMap.render(cubeMapShader);
        }

        if(ui.begin()) {
            nk_layout_row_dynamic(ctx, 20, 2);
            if (nk_button_label(ctx, "Open Model")) {
                CharSequence charSequence = new StringBuffer("jpg");
                String modelPath = tinyfd_openFileDialog("Open a 3D Model File", "", null, "", false);
                if(modelPath != null && model.loadMesh(modelPath))
                    camera.setPosition(new Vector3f(0.0f, 0.0f, model.getDistance()));
            }
            if (nk_button_label(ctx, "About")) {
                info.show();
                isInfoShowing = !isInfoShowing;
            }
            sidebar.renderUi();
            nk_layout_row_dynamic(ctx, 176, 1);
            if (nk_group_begin_titled(ctx, "Background", "Background", NK_WINDOW_BORDER | NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_TITLE)) {
                nk_layout_row_dynamic(ctx, 20, 1);
                if (nk_option_label(ctx, "Use Background Color", background == -1)) {
                    background = -1;
                    setClearColor(cc.get());
                }
                nk_layout_row_dynamic(ctx, 20, 1);
                if (nk_option_label(ctx, "Use Environment Map 1", background == 0)) {
                    background = 0;
                    cubeMap.activeCubeMapIndex = background;
                }
                nk_layout_row_dynamic(ctx, 20, 1);
                if (nk_option_label(ctx, "Use Environment Map 2", background == 1)) {
                    background = 1;
                    cubeMap.activeCubeMapIndex = background;
                }
                cc.renderUi();
                nk_group_end(ctx);
            }
            model.renderUi();
        }
        ui.end();

        if (sidebar.begin()) {
            model.renderMaterialUi();
        }
        sidebar.end();
    }

    @Override
    public void clear() {
        camera.clear();
        shaderProgram.clear();
        model.clear();
        ui.clear();
        sidebar.clear();
        cc.clear();

        camera = null;
        shaderProgram = null;
        model = null;
        ui = null;
        sidebar = null;
        cc = null;

        super.clear();
    }

    public static void main(String[] args) {
        new ModelViewer3D(1000, 600, "3D Model Viewer").run();
    }
}
