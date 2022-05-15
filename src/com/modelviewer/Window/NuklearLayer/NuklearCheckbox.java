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

    public void renderUi() {
        nk_checkbox_flags_text(ctx, name, value, checkedValue);
    }

    public void set(float value) {
        if(value > 0) {
            this.value[0] = this.checkedValue;
        } else {
            this.value[0] = 0;
        }
    }

    public int get() {
        return value[0];
    }

    public boolean isChecked() {
        return value[0] > 0;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addMaterialName(String materialName) {
        this.name += " (" + materialName + ")";
    }

    public void clear() {
        ctx = null;
        value = null;
        name = null;
    }
}
