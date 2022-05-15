package com.modelviewer.Tests;

import com.modelviewer.Camera.Camera;
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

public class NFDTest extends Window {
    private Camera camera;
    private ShaderProgram shaderProgram;
    private Model model;

    public NFDTest(int width, int height, String title) {
        super(width, height, title);
    }

    private NuklearLayer ui;
    private NuklearSideBar sidebar;
    private NuklearColor cc;

    @Override
    public void setup() {
        shaderProgram = new ShaderProgram("shaders/default.vs.glsl", "shaders/default.fs.glsl");
        shaderProgram.compile();

        model = new Model(ctx);

        camera = new Camera();
        camera.setPosition(new Vector3f(0.0f, 0.0f, model.getDistance()));

        ui = new NuklearLayer(
                ctx,
                "NDF Test",
                Utils.createNkRect(30, 30, 230, 300),
                NK_WINDOW_BORDER | NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE | NK_WINDOW_TITLE | NK_WINDOW_SCALABLE
        );

        sidebar = new NuklearSideBar(ctx, 300, width, height);
        cc = new NuklearColor(ctx, "Clear Color", new Vector4f(0.0f), "");

        ApplyTheme.apply(ctx, NuklearLayerTheme.RED);

        Constants.nullTexture = new Texture();
        Constants.nullTexture.init("resources/null.jpg", false, false);
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

        model.updateRotation(dt);
        model.render(shaderProgram);

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
        }
        ui.end();

        if (sidebar.begin()) {
            cc.renderUi();
            setClearColor(cc.get());
            model.renderUi();
        }
        sidebar.end();
    }

    public static void main(String[] args) {
        new NFDTest(1000, 600, "NDF Test").run();
    }
}
