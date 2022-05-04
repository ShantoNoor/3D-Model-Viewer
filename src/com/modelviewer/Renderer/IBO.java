package com.modelviewer.Renderer;

import com.modelviewer.Utils.Utils;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL15.*;

public class IBO {
    private int id;
    private int count;

    public IBO() {
        id = glGenBuffers();
    }

    public void bind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id);
    }

    public void unbind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void uploadIndicesData(VAO vao, ArrayList<Integer> indicesArray, BufferDataType bufferDataType) {
        vao.bind();
        bind();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, Utils.convertIntArrayListToIntBuffer(indicesArray), bufferDataType.getValue());
        unbind();
        vao.unbind();

        count = indicesArray.size();
    }

    public void uploadIndicesData(VAO vao, int[] indicesArray, BufferDataType bufferDataType) {
        vao.bind();
        bind();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, Utils.convertIntArrayToIntBuffer(indicesArray), bufferDataType.getValue());
        unbind();
        vao.unbind();

        count = indicesArray.length;
    }

    public int getCount() {
        return count;
    }

    public void clear() {
        glDeleteBuffers(id);
    }
}
