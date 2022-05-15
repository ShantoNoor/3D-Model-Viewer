package com.modelviewer.Window.NuklearLayer;

import com.modelviewer.Renderer.Shader.ShaderProgram;
import org.joml.Vector4f;
import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.nuklear.Nuklear.*;

public class NuklearColor {
    private NkContext ctx;
    private NkColorf colorf;
    private NkColor color;
    private String name, shaderVarName;
    private NkVec2 vec2;

    public NuklearColor(NkContext ctx, String name, Vector4f color, String shaderVarName) {
        this.ctx = ctx;
        this.name = name;
        this.colorf = NkColorf.create().r(color.x).g(color.y).b(color.z).a(color.w);
        this.vec2 = NkVec2.create().x(nk_window_get_width(ctx)).y(400);
        this.shaderVarName = shaderVarName;
        this.color = NkColor.create();
    }

    public void renderUi() {
        nk_layout_row_dynamic(ctx, 60, 1);
        if(nk_group_begin(ctx, name, NK_WINDOW_BORDER | NK_WINDOW_NO_SCROLLBAR)) {
            nk_layout_row_dynamic(ctx, 20, 1);
            nk_label(ctx, name + ":", NK_TEXT_LEFT);

            nk_layout_row_dynamic(ctx, 25, 1);
            if (nk_combo_begin_color(ctx, nk_rgb_cf(colorf, color), vec2.set(nk_widget_width(ctx), 400))) {
                nk_layout_row_dynamic(ctx, 120, 1);
                nk_color_picker(ctx, colorf, NK_RGBA);
                nk_layout_row_dynamic(ctx, 25, 1);
                colorf
                        .r(nk_propertyf(ctx, "#R:", 0, colorf.r(), 1.0f, 0.01f, 0.005f))
                        .g(nk_propertyf(ctx, "#G:", 0, colorf.g(), 1.0f, 0.01f, 0.005f))
                        .b(nk_propertyf(ctx, "#B:", 0, colorf.b(), 1.0f, 0.01f, 0.005f))
                        .a(nk_propertyf(ctx, "#A:", 0, colorf.a(), 1.0f, 0.01f, 0.005f));
                nk_combo_end(ctx);
            }

            nk_group_end(ctx);
        }
    }

    public Vector4f get() {
        return new Vector4f(colorf.r(), colorf.g(), colorf.b(), colorf.a());
    }

    public void set(Vector4f color) {
        this.colorf.r(color.x).g(color.y).b(color.z).a(color.w);
    }

    public void upload(ShaderProgram shaderProgram) {
        shaderProgram.upload(shaderVarName, get());
    }

    public void safeUpload(ShaderProgram shaderProgram) {
        shaderProgram.safeUpload(shaderVarName, get());
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addMaterialName(String materialName) {
        this.name += " (" + materialName + ")";
    }

    public void clear() {
        ctx = null;

        colorf.clear();
        colorf = null;

        color.clear();
        color = null;

        name = null;
        shaderVarName = null;

        vec2.clear();
        vec2 = null;
    }
}
