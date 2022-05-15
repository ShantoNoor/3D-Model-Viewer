package com.modelviewer.Renderer;
import com.modelviewer.Renderer.Shader.ShaderProgram;

import static org.lwjgl.opengl.GL11.*;

public class Quad {
    private VAO vao;
    private VBO vbo;
    private IBO ibo;

    private float val = 1.0f;
    private float zVal = 0.9999f;
    private float[] vertexArray = {
             // positions
             val, -val, zVal,
            -val,  val, zVal,
             val,  val, zVal,
            -val, -val, zVal
    };
    private int[] elementArray = {
            2, 1, 0,
            0, 1, 3
    };

    public Quad() {
        vao = new VAO();
        vbo = new VBO();
        ibo = new IBO();

        int positionLenght = 3;
        int vertexSize = positionLenght * 4;

        ibo.uploadIndicesData(vao, elementArray, BufferDataType.STATIC);
        vbo.uploadVertexAttributeData(vao, vertexArray, 0, positionLenght, vertexSize,
                0, BufferDataType.STATIC);
    }

    public void render(ShaderProgram shaderProgram) {
        shaderProgram.bind();
        vao.bind();
        ibo.bind();
        glDrawElements(GL_TRIANGLES, ibo.getCount(), GL_UNSIGNED_INT, 0);
        ibo.unbind();
        vao.unbind();
    }

    public void clear() {
        vao.clear();
        vbo.clear();
        ibo.clear();

        vertexArray = null;
        elementArray = null;
    }
}
