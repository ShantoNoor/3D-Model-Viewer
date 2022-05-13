package com.modelviewer.Window.NuklearLayer;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.Nuklear;

import static org.lwjgl.nuklear.Nuklear.*;

public class NuklearLayer {
    protected NkContext ctx;
    protected NkRect rect;
    protected String title;
    protected int flags;

    public NuklearLayer(NkContext ctx, String title, NkRect rect, int flags) {
        this.ctx = ctx;
        this.title = title;
        this.flags = flags;
        this.rect = rect;

        if((flags & NK_WINDOW_CLOSABLE) > 0) {
            begin();
            end();
            Nuklear.nk_window_show(ctx, title, 0);
        }
    }

    public boolean begin() {
        return nk_begin(ctx, title, rect, flags);
    }

    public void end() {
        nk_end(ctx);
    }

    public void show() {
        Nuklear.nk_window_show(ctx, title, 1);
    }
}
