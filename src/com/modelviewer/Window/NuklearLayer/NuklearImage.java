package com.modelviewer.Window.NuklearLayer;

import com.modelviewer.Renderer.Texture;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkImage;

import static org.lwjgl.nuklear.Nuklear.*;

public class NuklearImage {
    NkContext ctx;
    NkImage image;
    Texture texture;
    NuklearCheckbox use;

    public void updateAndRender() {nk_layout_row_dynamic(ctx, 285, 1);
        if (nk_group_begin_titled(ctx, "My Group", "Normal Map", NK_WINDOW_BORDER | NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_TITLE)) {
            nk_layout_row_begin(ctx, NK_DYNAMIC, nk_window_get_width(ctx) - 90, 2);
            nk_layout_row_push(ctx, 0.8f);
            nk_button_image(ctx, image);
            nk_layout_row_push(ctx, 0.2f);
            use.updateAndRenderUi();

            nk_layout_row_dynamic(ctx, 25, 2);
            if (nk_button_label(ctx, "Select")) {

            }
            if (nk_button_label(ctx, "Clear")) {

            }
            nk_group_end(ctx);
        }
    }
}
