package com.modelviewer.Renderer;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.*;

public class VAO {
    private int id;
    private ArrayList<Integer> vertexAttributes;

    public VAO() {
        id = glGenVertexArrays();
        vertexAttributes = new ArrayList<Integer>();
    }

    public void bind() {
        for(int i = 0; i < vertexAttributes.size(); ++i) {
            enableVertexAttribute(vertexAttributes.get(i));
        }
        glBindVertexArray(id);
    }

    public void unbind() {
        for(int i = 0; i < vertexAttributes.size(); ++i) {
            disableVertexAttribute(vertexAttributes.get(i));
        }
        glBindVertexArray(0);
    }

    public void enableVertexAttribute(int attributeIndex) {
        glEnableVertexAttribArray(attributeIndex);
    }

    public void disableVertexAttribute(int attributeIndex) {
        glDisableVertexAttribArray(attributeIndex);
    }

    public void updateVertexAttributePointer(int attributeIndex, int singleVertexAttributeSize,
                                             int vertexAttributeStartOffset, int nextVertexAttributeOffsetSize) {
        addVertexAttribute(attributeIndex);
        enableVertexAttribute(attributeIndex);
        glVertexAttribPointer(attributeIndex, singleVertexAttributeSize, GL_FLOAT, false,
                vertexAttributeStartOffset, nextVertexAttributeOffsetSize);
    }

    public int getId() {
        return id;
    }

    public void addVertexAttribute(int attributeIndex) {
        vertexAttributes.add(attributeIndex);
    }

    public void clear() {
        vertexAttributes.clear();
        glDeleteVertexArrays(id);
    }
}
