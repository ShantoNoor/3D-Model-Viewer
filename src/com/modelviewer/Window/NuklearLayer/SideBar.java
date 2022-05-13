package com.modelviewer.Window.NuklearLayer;

import com.modelviewer.Window.Input.MouseListener;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.Nuklear;

public class SideBar extends NuklearLayer{
    private float sideBarWidth;
    private float maxSideBarWidth;
    public boolean show = false;

    public SideBar(NkContext ctx, String title, NkRect rect, int flags) {
        super(ctx, title, rect, flags);
        maxSideBarWidth = rect.w();
        sideBarWidth = 0.0f;
        rect.x(rect.x()+maxSideBarWidth);
    }

    public void update(float dt) {
        dt *= 1000;
        if(show && sideBarWidth < maxSideBarWidth) {
            sideBarWidth += dt;
            rect.x(rect.x()-dt);
        }

        if(!show && sideBarWidth > 0.0f) {
            sideBarWidth -= dt;
            rect.x(rect.x()+dt);
        }

        if(MouseListener.isAnyClickOverMainGlfwWindow()) {
            show = false;
        }
    }
}
