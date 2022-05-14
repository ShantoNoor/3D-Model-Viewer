package com.modelviewer.Tests;

import com.modelviewer.Camera.Camera;
import com.modelviewer.Renderer.Mesh.Model;
import com.modelviewer.Renderer.Shader.ShaderProgram;
import com.modelviewer.Renderer.Texture;
import com.modelviewer.Utils.Constants;
import com.modelviewer.Utils.Utils;
import com.modelviewer.Window.NuklearLayer.*;
import com.modelviewer.Window.Window;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.nuklear.Nuklear.nk_label;
import static org.lwjgl.util.tinyfd.TinyFileDialogs.*;

public class NFDTest extends Window {
    private Camera camera;
    private ShaderProgram shaderProgram;
    private Model model;
    private Texture baseColor;
    private Texture normalMap;
    private Texture aoMap;
    private Texture metalnessMap;
    private Texture roughnessMap;

//    private final Calculator calc = new Calculator();

    public NFDTest(int width, int height, String title) {
        super(width, height, title);
    }

    Matrix4f rotate;

    private NuklearLayer ui;
    private SideBar sidebar;
    private NuklearColor cc;

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

//        model.loadMesh("TestModels/Drawing7.stl");

        camera = new Camera();
        camera.setPosition(new Vector3f(0.0f, 0.0f, model.getDistance()));

        rotate = new Matrix4f();

        ui = new NuklearLayer(
                ctx,
                "NDF Test",
                Utils.createNkRect(30, 30, 230, 550),
                NK_WINDOW_BORDER | NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE | NK_WINDOW_TITLE | NK_WINDOW_SCALABLE
        );

        sidebar = new SideBar(ctx, 300, width, height);
        cc = new NuklearColor(ctx, "Clear Color", new Vector4f(0.0f));

        ApplyTheme.apply(ctx, NuklearLayerTheme.RED);

        Constants.nullTexture = new Texture();
        Constants.nullTexture.init("resources/null.jpg");
    }

    NuklearFloat shineness = new NuklearFloat(ctx, "Shineness", 56.0f, 0.0f, 100.0f, 0.5f, 0.1f);
    NuklearCheckbox use = new NuklearCheckbox(ctx, "Use");


    @Override
    public void loop(float dt) {

        camera.mouseUpdate(glfwWindow, width, height);
        camera.keyboardUpdate(dt);

        shaderProgram.safeUpload("projection", projectionMatrix);
        shaderProgram.safeUpload("view", camera.getViewMatrix());
        shaderProgram.safeUpload("camPos", camera.getPosition());
        shaderProgram.safeUpload("lightDir", new Vector3f(0, 110.0f, 110.0f));
        shaderProgram.safeUpload("rotate", rotate.rotate(dt, Constants.Y_AXIS));

        baseColor.bind(shaderProgram, "baseColor", 0);
        normalMap.bind(shaderProgram, "normalMap", 1);
        aoMap.bind(shaderProgram, "aoMap", 2);
        metalnessMap.bind(shaderProgram, "metalnessMap", 3);
        roughnessMap.bind(shaderProgram, "roughnessMap", 4);

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
//                    tinyfd_notifyPopup("PopUp", "Hi", "warning");
//                    System.out.println(tinyfd_messageBox("box", "hi", "yesno", "warning", true));
                    System.out.println("about");
                }
                sidebar.renderUi();
                shineness.updateAndRenderUi();
            }
            ui.end();

            if (sidebar.begin()) {
                nk_layout_row_dynamic(ctx, 800, 1);
                if(nk_group_begin(ctx, "kk", NK_WINDOW_BORDER | NK_WINDOW_NO_SCROLLBAR)) {
                    cc.updateAndRenderUi(stack);
                    setClearColor(cc.get());
                    model.renderUi(ctx);

                    NkImage image = NkImage.create();
                    image.handle(it -> it.id(Constants.nullTexture.getId()));

                    nk_layout_row_dynamic(ctx, 285, 1);
                    if (nk_group_begin_titled(ctx, "My Group", "Normal Map", NK_WINDOW_BORDER | NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_TITLE)) {
                        nk_layout_row_begin(ctx, NK_DYNAMIC, nk_window_get_width(ctx) - 90, 2);
                        nk_layout_row_push(ctx, 0.8f);
                        nk_button_image(ctx, image);
                        nk_layout_row_push(ctx, 0.2f);

                        if(false) use.updateAndRenderUi();

                        nk_layout_row_dynamic(ctx, 25, 2);
                        if (nk_button_label(ctx, "Select")) {

                        }
                        if (nk_button_label(ctx, "Clear")) {

                        }
                        nk_group_end(ctx);
                    }
                    nk_group_end(ctx);
                }
            }
            sidebar.end();
        }
    }

    public static void main(String[] args) {
        new NFDTest(1000, 600, "NDF Test").run();
    }
}
