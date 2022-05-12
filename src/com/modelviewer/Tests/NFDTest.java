package com.modelviewer.Tests;

import com.modelviewer.Camera.Camera;
import com.modelviewer.Renderer.Mesh.Model;
import com.modelviewer.Renderer.Shader.ShaderProgram;
import com.modelviewer.Renderer.Texture;
import com.modelviewer.Utils.Constants;
import com.modelviewer.Utils.Utils;
import com.modelviewer.Window.NuklearLayer.NuklearLayer;
import com.modelviewer.Window.NuklearLayer.ApplyTheme;
import com.modelviewer.Window.NuklearLayer.NuklearLayerTheme;
import com.modelviewer.Window.Window;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkColorf;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.nuklear.Nuklear.*;
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
    public NkColorf background;

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
                Utils.createNkRect(30, 30, 230, 250),
                NK_WINDOW_BORDER | NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE | NK_WINDOW_TITLE | NK_WINDOW_SCALABLE
        );

        background = NkColorf.create().r(0.10f).g(0.18f).b(0.24f).a(1.0f);

        ApplyTheme.apply(ctx, NuklearLayerTheme.RED);
//        ctx.style().window().fixed_background().data().color().set((byte) 44, (byte) 44, (byte) 44, (byte) 200);
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

        try(MemoryStack stack = MemoryStack.stackPush()) {
            if(ui.begin()) {
                nk_layout_row_dynamic(ctx, 0, 2);
                if (nk_button_label(ctx, "Load")) {
                    CharSequence charSequence = new StringBuffer("jpg");
                    String modelPath = tinyfd_openFileDialog("Open a 3D Model File", "", null, "", false);
                    if(modelPath != null && model.loadMesh(modelPath))
                        camera.setPosition(new Vector3f(0.0f, 0.0f, model.getDistance()));
                }
                if (nk_button_label(ctx, "About")) {
                    info.show();
//                    tinyfd_notifyPopup("PopUp", "Hi", "warning");
                    System.out.println(tinyfd_messageBox("box", "hi", "yesno", "warning", true));
                }

                nk_layout_row_dynamic(ctx, 0, 1);
                nk_label(ctx, "background:", NK_TEXT_LEFT);
                nk_layout_row_dynamic(ctx, 25, 1);
                if (nk_combo_begin_color(ctx, nk_rgb_cf(background, NkColor.malloc(stack)), NkVec2.malloc(stack).set(nk_widget_width(ctx), 400))) {
                    nk_layout_row_dynamic(ctx, 120, 1);
                    nk_color_picker(ctx, background, NK_RGBA);
                    nk_layout_row_dynamic(ctx, 25, 1);
                    background
                            .r(nk_propertyf(ctx, "#R:", 0, background.r(), 1.0f, 0.01f, 0.005f))
                            .g(nk_propertyf(ctx, "#G:", 0, background.g(), 1.0f, 0.01f, 0.005f))
                            .b(nk_propertyf(ctx, "#B:", 0, background.b(), 1.0f, 0.01f, 0.005f))
                            .a(nk_propertyf(ctx, "#A:", 0, background.a(), 1.0f, 0.01f, 0.005f));
                    nk_combo_end(ctx);
                }
                setClearColor(new Vector4f(background.r(), background.g(), background.b(), background.a()));
            }
            ui.end();
        }
    }

    public static void main(String[] args) {
        new NFDTest(1000, 600, "NDF Test").run();
    }
}
