package com.modelviewer.Renderer.Shader;


import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL40.GL_TESS_CONTROL_SHADER;
import static org.lwjgl.opengl.GL40.GL_TESS_EVALUATION_SHADER;
import static org.lwjgl.opengl.GL43.GL_COMPUTE_SHADER;

public enum ShaderType {
    VERTEX(GL_VERTEX_SHADER, "Vertex"),
    FRAGMENT(GL_FRAGMENT_SHADER, "Fragment"),
    TESSELLATION_CONTROL(GL_TESS_CONTROL_SHADER, "Tessellation Control"),
    TESSELLATION_EVAL(GL_TESS_EVALUATION_SHADER, "Tessellation Eval"),
    GEOMETRY(GL_GEOMETRY_SHADER, "Geometry"),
    COMPUTE(GL_COMPUTE_SHADER, "Compute")
    ;

    private int value;
    private String name;

    private ShaderType(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name;
    }
}
