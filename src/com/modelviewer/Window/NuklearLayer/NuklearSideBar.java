package com.modelviewer.Window.NuklearLayer;

import com.modelviewer.Utils.Utils;
import com.modelviewer.Window.Input.MouseListener;
import org.lwjgl.nuklear.NkContext;

import static org.lwjgl.nuklear.Nuklear.*;

public class NuklearSideBar extends NuklearLayer{
    private float sideBarWidth;
    private float maxSideBarWidth;
    private boolean show = false;
    private NuklearCheckbox lock;

    public NuklearSideBar(NkContext ctx, float sideBarWidth, float windowWidth, float windowHeight) {
        super(ctx, "", Utils.createNkRect(windowWidth, 0, sideBarWidth, windowHeight), 0);
        maxSideBarWidth = sideBarWidth;
        this.sideBarWidth = 0.0f;
        this.lock = new NuklearCheckbox(ctx, "Lock Menu");
    }

    @Override
    public boolean begin() {
        update();
        return super.begin();
    }

    public void updateSize(int windowWidth, int windowHeight) {
        rect.h(windowHeight);
        rect.x(windowWidth+maxSideBarWidth - (2 * sideBarWidth));
    }

    private void update() {
        float speed = 20;
        if(show && sideBarWidth < maxSideBarWidth) {
            sideBarWidth += speed;
            rect.x(rect.x()-speed);
        }

        if(!show && sideBarWidth > 0.0f) {
            sideBarWidth -= speed;
            rect.x(rect.x()+speed);
        }

        if(MouseListener.isAnyClickOverMainGlfwWindow() && !lock.isChecked()) {
            show = false;
        }
    }

    public void renderUi() {
        nk_layout_row_dynamic(ctx, 20, 2);
        if(nk_button_label(ctx, "Edit Menu")) {
            show = !show;
        }
        lock.renderUi();
    }
}
