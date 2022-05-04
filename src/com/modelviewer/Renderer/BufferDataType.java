package com.modelviewer.Renderer;

import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public enum BufferDataType {
    STATIC(GL_STATIC_DRAW),
    DYNAMIC(GL_DYNAMIC_DRAW);

    private int value;
    private BufferDataType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
