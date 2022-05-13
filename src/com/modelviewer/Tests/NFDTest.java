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
import com.modelviewer.Window.NuklearLayer.SideBar;
import com.modelviewer.Window.Window;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.nuklear.*;
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
        private SideBar sidebar;
    public NkColorf background;
    private int sideBarWidth = 300;

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

        sidebar = new SideBar(
                ctx,
                "Side Bar",
                Utils.createNkRect(width-sideBarWidth, 0, sideBarWidth, height),
                0
        );

        background = NkColorf.create().r(0.10f).g(0.18f).b(0.24f).a(1.0f);
//
        ApplyTheme.apply(ctx, NuklearLayerTheme.RED);
//        ctx.style().window().fixed_background().data().color().set((byte) 44, (byte) 44, (byte) 44, (byte) 200);
    }

    float[] value = new float[1];

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

        shaderProgram.safeUpload("shine", value[0]);

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
                }

                nk_layout_row_dynamic(ctx, 20, 2);
                if(nk_button_label(ctx, "Show SideBar")) {
                    sidebar.show = true;
                }
                if(nk_button_label(ctx, "Close SideBar")) {
                    sidebar.show = false;
                }

//
//                nk_layout_row_dynamic(ctx, 20, 1);
//                nk_label(ctx, "background:", NK_TEXT_LEFT);
//                nk_layout_row_dynamic(ctx, 25, 1);
//                if (nk_combo_begin_color(ctx, nk_rgb_cf(background, NkColor.malloc(stack)), NkVec2.malloc(stack).set(nk_widget_width(ctx), 400))) {
//                    nk_layout_row_dynamic(ctx, 120, 1);
//                    nk_color_picker(ctx, background, NK_RGBA);
//                    nk_layout_row_dynamic(ctx, 25, 1);
//                    background
//                            .r(nk_propertyf(ctx, "#R:", 0, background.r(), 1.0f, 0.01f, 0.005f))
//                            .g(nk_propertyf(ctx, "#G:", 0, background.g(), 1.0f, 0.01f, 0.005f))
//                            .b(nk_propertyf(ctx, "#B:", 0, background.b(), 1.0f, 0.01f, 0.005f))
//                            .a(nk_propertyf(ctx, "#A:", 0, background.a(), 1.0f, 0.01f, 0.005f));
//                    nk_combo_end(ctx);
//                }

//                nk_layout_row_dynamic(ctx, 100, 1);
//                NkImage image = NkImage.create();
//                image.handle(it -> it.id(0));
//                nk_button_image_label(ctx, image, "Normal Map", NK_TEXT_ALIGN_TOP);
//                nk_layout_row_dynamic(ctx, 0, 1);
//                nk_draw_image(Nuklear.nk_window_get_canvas(ctx), Utils.createNkRect(20, 20, 100, 100), image, Utils.createNkColor(255, 212, 214, 255));
//                nk_layout_row_dynamic(ctx, 0, 1);
//                nk_contextual_item_image_label(ctx, image, "nor", NK_TEXT_RIGHT);
//                nk_layout_row_dynamic(ctx, 0, 1);
//                nk_combo_begin_image_label(ctx, "image", image, Utils.createNkVec2(100, 500));
//                nk_layout_row_dynamic(ctx, nk_widget_width(ctx), 1);
//                nk_button_image(ctx, image);
//                nk_button_image_styled();

//                nk_layout_row_dynamic(ctx, nk_widget_width(ctx)+50, 1);
//                if(nk_group_begin(ctx, "My Group", 0)) {
//                    nk_layout_row_dynamic(ctx, 40, 1);
//                    nk_button_label(ctx, "NormalMap");
//                    nk_layout_row_dynamic(ctx, nk_widget_width(ctx), 1);
//                    nk_button_image(ctx, image);
//                    nk_group_end(ctx);
//                }
                nk_layout_row_dynamic(ctx, 180, 1);
                if(nk_group_begin(ctx, "Shineness", NK_WINDOW_BORDER | NK_WINDOW_NO_SCROLLBAR)) {
                    nk_layout_row_dynamic(ctx, 20, 1);
                    nk_label(ctx, "Shineness: ", NK_TEXT_LEFT);
                    nk_layout_row_begin(ctx, NK_STATIC, 20, 2);
                    nk_layout_row_push(ctx, nk_window_get_width(ctx)-115);
                    nk_slider_float(ctx, 0.1f, value, 100.0f, 0.25f);
                    nk_layout_row_push(ctx, 80.0f);
                    nk_property_float(ctx, "", 0.1f, value, 95.0f, 0.25f, 0.01f);
                    nk_layout_row_end(ctx);
                    nk_layout_row_dynamic(ctx, 120, 1);
                    nk_color_picker(ctx, background, NK_RGBA);
                    nk_group_end(ctx);
                }
                setClearColor(new Vector4f(background.r(), background.g(), background.b(), background.a()));
            }
            ui.end();
        }
//        System.out.println(value[0]);

        sidebar.update(dt);
        try(MemoryStack stack = MemoryStack.stackPush()) {
            if (sidebar.begin()) {
                model.ui(ctx);

            }
            sidebar.end();
        }
    }

    public static void main(String[] args) {
        new NFDTest(1000, 600, "NDF Test").run();
    }
}
