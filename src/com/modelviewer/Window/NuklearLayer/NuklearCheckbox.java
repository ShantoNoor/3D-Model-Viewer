package com.modelviewer.Window.NuklearLayer;

import org.lwjgl.nuklear.NkContext;

import static org.lwjgl.nuklear.Nuklear.nk_checkbox_flags_text;

public class NuklearCheckbox {
    private NkContext ctx;
    private String name;
    private int checkedValue = 1;
    private int value[] = new int[1];

    public NuklearCheckbox(NkContext ctx, String name, int defaultValue) {
        this.ctx = ctx;
        this.name = name;
        this.checkedValue = defaultValue;
    }

    public NuklearCheckbox(NkContext ctx, String name) {
        this.ctx = ctx;
        this.name = name;
    }

    public void updateAndRenderUi() {
        nk_checkbox_flags_text(ctx, name, value, checkedValue);
    }

    public void set(int value) {
        this.value[0] = value;
    }

    public int get() {
        return value[0];
    }

    public boolean isChecked() {
        return value[0] > 0;
    }
}
