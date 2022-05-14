package com.modelviewer.Window.NuklearLayer;

import com.modelviewer.Renderer.Shader.ShaderProgram;
import org.lwjgl.nuklear.NkContext;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_end;

public class NuklearFloat {
    private NkContext ctx;
    private float[] value = new float[1];
    private String name, shaderVarName;
    float minValue, maxValue, step, inc_per_pixel;

    public NuklearFloat(NkContext ctx, String name, float value, float minValue, float maxValue, float step, float inc_per_pixel, String shaderVarName) {
        this.ctx = ctx;
        this.name = name;
        this.value[0] = value;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.step = step;
        this.inc_per_pixel = inc_per_pixel;
        this.shaderVarName = shaderVarName;
    }

    public float get() {
        return value[0];
    }

    public void set(float value) {
        this.value[0] = value;
    }

    public void updateAndRenderUi() {
        nk_layout_row_dynamic(ctx, 60, 1);
        if(nk_group_begin(ctx, name, NK_WINDOW_BORDER | NK_WINDOW_NO_SCROLLBAR)) {
            nk_layout_row_dynamic(ctx, 20, 1);
            nk_label(ctx, name + ":", NK_TEXT_LEFT);
            nk_layout_row_begin(ctx, NK_STATIC, 20, 2);
            nk_layout_row_push(ctx, nk_window_get_width(ctx) - 125);
            nk_slider_float(ctx, minValue, value, maxValue, step);
            nk_layout_row_push(ctx, 80.0f);
            nk_property_float(ctx, "", minValue, value, maxValue, step, inc_per_pixel);
            nk_layout_row_end(ctx);
            nk_group_end(ctx);
        }
    }

    public void upload(ShaderProgram shaderProgram) {
        shaderProgram.upload(shaderVarName, value[0]);
    }

    public void safeUpload(ShaderProgram shaderProgram) {
        shaderProgram.safeUpload(shaderVarName, value[0]);
    }
}
