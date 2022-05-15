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
        glBindVertexArray(id);
        for(int i = 0; i < vertexAttributes.size(); ++i) {
            enableVertexAttribute(vertexAttributes.get(i));
        }
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

    public void updateVertexAttributePointer(int attributeIndex, int singleVertexAttributeLength,
                                             int singleVertexSizeInBytes, int singleVertexAttributeStartingOffsetSizeInBytes) {
        addVertexAttribute(attributeIndex);
        enableVertexAttribute(attributeIndex);
        glVertexAttribPointer(attributeIndex, singleVertexAttributeLength, GL_FLOAT, false,
                singleVertexSizeInBytes, singleVertexAttributeStartingOffsetSizeInBytes);
    }

    public int getId() {
        return id;
    }

    public void addVertexAttribute(int attributeIndex) {
        vertexAttributes.add(attributeIndex);
    }

    public void clear() {
        vertexAttributes.clear();
        vertexAttributes = null;
        glDeleteVertexArrays(id);
    }
}
