package com.modelviewer.Window.NuklearLayer;

import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static org.lwjgl.nuklear.Nuklear.*;

public class ApplyTheme {
    public static void apply(NkContext context, NuklearLayerTheme nuklearLayerTheme) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            int size = NkColor.SIZEOF * NK_COLOR_COUNT;
            ByteBuffer buffer = stack.calloc(size);
            NkColor.Buffer colors = new NkColor.Buffer(buffer);
            if(nuklearLayerTheme == NuklearLayerTheme.WHITE) {
                colors.put(NK_COLOR_TEXT, createColor(stack, 70, 70, 70, 255));
                colors.put(NK_COLOR_WINDOW, createColor(stack, 175, 175, 175, 255));
                colors.put(NK_COLOR_HEADER, createColor(stack, 175, 175, 175, 255));
                colors.put(NK_COLOR_BORDER, createColor(stack, 0, 0, 0, 255));
                colors.put(NK_COLOR_BUTTON, createColor(stack, 185, 185, 185, 255));
                colors.put(NK_COLOR_BUTTON_HOVER, createColor(stack, 170, 170, 170, 255));
                colors.put(NK_COLOR_BUTTON_ACTIVE, createColor(stack, 160, 160, 160, 255));
                colors.put(NK_COLOR_TOGGLE, createColor(stack, 150, 150, 150, 255));
                colors.put(NK_COLOR_TOGGLE_HOVER, createColor(stack, 120, 120, 120, 255));
                colors.put(NK_COLOR_TOGGLE_CURSOR, createColor(stack, 175, 175, 175, 255));
                colors.put(NK_COLOR_SELECT, createColor(stack, 190, 190, 190, 255));
                colors.put(NK_COLOR_SELECT_ACTIVE, createColor(stack, 175, 175, 175, 255));
                colors.put(NK_COLOR_SLIDER, createColor(stack, 190, 190, 190, 255));
                colors.put(NK_COLOR_SLIDER_CURSOR, createColor(stack, 80, 80, 80, 255));
                colors.put(NK_COLOR_SLIDER_CURSOR_HOVER, createColor(stack, 70, 70, 70, 255));
                colors.put(NK_COLOR_SLIDER_CURSOR_ACTIVE, createColor(stack, 60, 60, 60, 255));
                colors.put(NK_COLOR_PROPERTY, createColor(stack, 175, 175, 175, 255));
                colors.put(NK_COLOR_EDIT, createColor(stack, 150, 150, 150, 255));
                colors.put(NK_COLOR_EDIT_CURSOR, createColor(stack, 0, 0, 0, 255));
                colors.put(NK_COLOR_COMBO, createColor(stack, 175, 175, 175, 255));
                colors.put(NK_COLOR_CHART, createColor(stack, 160, 160, 160, 255));
                colors.put(NK_COLOR_CHART_COLOR, createColor(stack, 45, 45, 45, 255));
                colors.put(NK_COLOR_CHART_COLOR_HIGHLIGHT, createColor(stack,  255, 0, 0, 255));
                colors.put(NK_COLOR_SCROLLBAR, createColor(stack, 180, 180, 180, 255));
                colors.put(NK_COLOR_SCROLLBAR_CURSOR, createColor(stack, 140, 140, 140, 255));
                colors.put(NK_COLOR_SCROLLBAR_CURSOR_HOVER, createColor(stack, 150, 150, 150, 255));
                colors.put(NK_COLOR_SCROLLBAR_CURSOR_ACTIVE, createColor(stack, 160, 160, 160, 255));
                colors.put(NK_COLOR_TAB_HEADER, createColor(stack, 180, 180, 180, 255));
            } else if(nuklearLayerTheme == NuklearLayerTheme.RED) {
                colors.put(NK_COLOR_TEXT, createColor(stack, 190, 190, 190, 255));
                colors.put(NK_COLOR_WINDOW, createColor(stack, 30, 33, 40, 215));
                colors.put(NK_COLOR_HEADER, createColor(stack, 181, 45, 69, 220));
                colors.put(NK_COLOR_BORDER, createColor(stack, 51, 55, 67, 255));
                colors.put(NK_COLOR_BUTTON, createColor(stack, 181, 45, 69, 255));
                colors.put(NK_COLOR_BUTTON_HOVER, createColor(stack, 190, 50, 70, 255));
                colors.put(NK_COLOR_BUTTON_ACTIVE, createColor(stack, 195, 55, 75, 255));
                colors.put(NK_COLOR_TOGGLE, createColor(stack, 51, 55, 67, 255));
                colors.put(NK_COLOR_TOGGLE_HOVER, createColor(stack, 45, 60, 60, 255));
                colors.put(NK_COLOR_TOGGLE_CURSOR, createColor(stack, 181, 45, 69, 255));
                colors.put(NK_COLOR_SELECT, createColor(stack, 51, 55, 67, 255));
                colors.put(NK_COLOR_SELECT_ACTIVE, createColor(stack, 181, 45, 69, 255));
                colors.put(NK_COLOR_SLIDER, createColor(stack, 51, 55, 67, 255));
                colors.put(NK_COLOR_SLIDER_CURSOR, createColor(stack, 181, 45, 69, 255));
                colors.put(NK_COLOR_SLIDER_CURSOR_HOVER, createColor(stack, 186, 50, 74, 255));
                colors.put(NK_COLOR_SLIDER_CURSOR_ACTIVE, createColor(stack, 191, 55, 79, 255));
                colors.put(NK_COLOR_PROPERTY, createColor(stack, 51, 55, 67, 255));
                colors.put(NK_COLOR_EDIT, createColor(stack, 51, 55, 67, 225));
                colors.put(NK_COLOR_EDIT_CURSOR, createColor(stack, 190, 190, 190, 255));
                colors.put(NK_COLOR_COMBO, createColor(stack, 51, 55, 67, 255));
                colors.put(NK_COLOR_CHART, createColor(stack, 51, 55, 67, 255));
                colors.put(NK_COLOR_CHART_COLOR, createColor(stack, 170, 40, 60, 255));
                colors.put(NK_COLOR_CHART_COLOR_HIGHLIGHT, createColor(stack,  255, 0, 0, 255));
                colors.put(NK_COLOR_SCROLLBAR, createColor(stack, 30, 33, 40, 255));
                colors.put(NK_COLOR_SCROLLBAR_CURSOR, createColor(stack, 64, 84, 95, 255));
                colors.put(NK_COLOR_SCROLLBAR_CURSOR_HOVER, createColor(stack, 70, 90, 100, 255));
                colors.put(NK_COLOR_SCROLLBAR_CURSOR_ACTIVE, createColor(stack, 75, 95, 105, 255));
                colors.put(NK_COLOR_TAB_HEADER, createColor(stack, 181, 45, 69, 220));
            } else if(nuklearLayerTheme == NuklearLayerTheme.BLUE) {
                colors.put(NK_COLOR_TEXT, createColor(stack, 20, 20, 20, 255));
                colors.put(NK_COLOR_WINDOW, createColor(stack, 202, 212, 214, 215));
                colors.put(NK_COLOR_HEADER, createColor(stack, 137, 182, 224, 220));
                colors.put(NK_COLOR_BORDER, createColor(stack, 140, 159, 173, 255));
                colors.put(NK_COLOR_BUTTON, createColor(stack, 137, 182, 224, 255));
                colors.put(NK_COLOR_BUTTON_HOVER, createColor(stack, 142, 187, 229, 255));
                colors.put(NK_COLOR_BUTTON_ACTIVE, createColor(stack, 147, 192, 234, 255));
                colors.put(NK_COLOR_TOGGLE, createColor(stack, 177, 210, 210, 255));
                colors.put(NK_COLOR_TOGGLE_HOVER, createColor(stack, 182, 215, 215, 255));
                colors.put(NK_COLOR_TOGGLE_CURSOR, createColor(stack, 137, 182, 224, 255));
                colors.put(NK_COLOR_SELECT, createColor(stack, 177, 210, 210, 255));
                colors.put(NK_COLOR_SELECT_ACTIVE, createColor(stack, 137, 182, 224, 255));
                colors.put(NK_COLOR_SLIDER, createColor(stack, 177, 210, 210, 255));
                colors.put(NK_COLOR_SLIDER_CURSOR, createColor(stack, 137, 182, 224, 245));
                colors.put(NK_COLOR_SLIDER_CURSOR_HOVER, createColor(stack, 142, 188, 229, 255));
                colors.put(NK_COLOR_SLIDER_CURSOR_ACTIVE, createColor(stack, 147, 193, 234, 255));
                colors.put(NK_COLOR_PROPERTY, createColor(stack, 210, 210, 210, 255));
                colors.put(NK_COLOR_EDIT, createColor(stack, 210, 210, 210, 225));
                colors.put(NK_COLOR_EDIT_CURSOR, createColor(stack, 20, 20, 20, 255));
                colors.put(NK_COLOR_COMBO, createColor(stack, 210, 210, 210, 255));
                colors.put(NK_COLOR_CHART, createColor(stack, 210, 210, 210, 255));
                colors.put(NK_COLOR_CHART_COLOR, createColor(stack, 137, 182, 224, 255));
                colors.put(NK_COLOR_CHART_COLOR_HIGHLIGHT, createColor(stack,  255, 0, 0, 255));
                colors.put(NK_COLOR_SCROLLBAR, createColor(stack, 190, 200, 200, 255));
                colors.put(NK_COLOR_SCROLLBAR_CURSOR, createColor(stack, 64, 84, 95, 255));
                colors.put(NK_COLOR_SCROLLBAR_CURSOR_HOVER, createColor(stack, 70, 90, 100, 255));
                colors.put(NK_COLOR_SCROLLBAR_CURSOR_ACTIVE, createColor(stack, 75, 95, 105, 255));
                colors.put(NK_COLOR_TAB_HEADER, createColor(stack, 156, 193, 220, 255));
            } else if(nuklearLayerTheme == NuklearLayerTheme.DARK) {
                colors.put(NK_COLOR_TEXT, createColor(stack, 210, 210, 210, 255));
                colors.put(NK_COLOR_WINDOW, createColor(stack, 57, 67, 71, 215));
                colors.put(NK_COLOR_HEADER, createColor(stack, 51, 51, 56, 220));
                colors.put(NK_COLOR_BORDER, createColor(stack, 46, 46, 46, 255));
                colors.put(NK_COLOR_BUTTON, createColor(stack, 48, 83, 111, 255));
                colors.put(NK_COLOR_BUTTON_HOVER, createColor(stack, 58, 93, 121, 255));
                colors.put(NK_COLOR_BUTTON_ACTIVE, createColor(stack, 63, 98, 126, 255));
                colors.put(NK_COLOR_TOGGLE, createColor(stack, 50, 58, 61, 255));
                colors.put(NK_COLOR_TOGGLE_HOVER, createColor(stack, 45, 53, 56, 255));
                colors.put(NK_COLOR_TOGGLE_CURSOR, createColor(stack, 48, 83, 111, 255));
                colors.put(NK_COLOR_SELECT, createColor(stack, 57, 67, 61, 255));
                colors.put(NK_COLOR_SELECT_ACTIVE, createColor(stack, 48, 83, 111, 255));
                colors.put(NK_COLOR_SLIDER, createColor(stack, 50, 58, 61, 255));
                colors.put(NK_COLOR_SLIDER_CURSOR, createColor(stack, 48, 83, 111, 245));
                colors.put(NK_COLOR_SLIDER_CURSOR_HOVER, createColor(stack, 53, 88, 116, 255));
                colors.put(NK_COLOR_SLIDER_CURSOR_ACTIVE, createColor(stack, 58, 93, 121, 255));
                colors.put(NK_COLOR_PROPERTY, createColor(stack, 50, 58, 61, 255));
                colors.put(NK_COLOR_EDIT, createColor(stack, 50, 58, 61, 225));
                colors.put(NK_COLOR_EDIT_CURSOR, createColor(stack, 210, 210, 210, 255));
                colors.put(NK_COLOR_COMBO, createColor(stack, 50, 58, 61, 255));
                colors.put(NK_COLOR_CHART, createColor(stack, 50, 58, 61, 255));
                colors.put(NK_COLOR_CHART_COLOR, createColor(stack, 48, 83, 111, 255));
                colors.put(NK_COLOR_CHART_COLOR_HIGHLIGHT, createColor(stack, 255, 0, 0, 255));
                colors.put(NK_COLOR_SCROLLBAR, createColor(stack, 50, 58, 61, 255));
                colors.put(NK_COLOR_SCROLLBAR_CURSOR, createColor(stack, 48, 83, 111, 255));
                colors.put(NK_COLOR_SCROLLBAR_CURSOR_HOVER, createColor(stack, 53, 88, 116, 255));
                colors.put(NK_COLOR_SCROLLBAR_CURSOR_ACTIVE, createColor(stack, 58, 93, 121, 255));
                colors.put(NK_COLOR_TAB_HEADER, createColor(stack, 48, 83, 111, 255));
            } else if(nuklearLayerTheme == NuklearLayerTheme.DEFAULT) {
                nk_style_default(context);
                return;
            }
            nk_style_from_table(context, colors);
        }
    }

    private static NkColor createColor(MemoryStack stack, int r, int g, int b, int a) {
        return NkColor.malloc(stack).set((byte) r, (byte) g, (byte) b, (byte) a);
    }
}
