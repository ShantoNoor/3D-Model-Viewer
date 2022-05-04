package com.modelviewer.Renderer;

import com.modelviewer.Utils.Utils;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL15.*;

public class VBO {
    private int id;

    public VBO() {
        id = glGenBuffers();
    }

    public void bind() {
        glBindBuffer(GL_ARRAY_BUFFER, id);
    }

    public void uploadVertexAttributeData(VAO vao, ArrayList<Float> data, int attributeIndex, int singleVertexAttributeLength,
                int singleVertexSizeInBytes, int singleVertexAttributeStartingOffsetSizeInBytes, BufferDataType bufferDataType) {
        vao.bind();
        bind();
        glBufferData(GL_ARRAY_BUFFER, Utils.convertFloatArrayListToFloatBuffer(data), bufferDataType.getValue());
        vao.updateVertexAttributePointer(attributeIndex, singleVertexAttributeLength, singleVertexSizeInBytes, singleVertexAttributeStartingOffsetSizeInBytes);
        unbind();
        vao.unbind();
    }

    public void uploadVertexAttributeData(VAO vao, float[] data, int attributeIndex, int singleVertexAttributeLength,
                int singleVertexSizeInBytes, int singleVertexAttributeStartingOffsetSizeInBytes, BufferDataType bufferDataType) {
        vao.bind();
        bind();
        glBufferData(GL_ARRAY_BUFFER, Utils.convertFloatArrayToFloatBuffer(data), bufferDataType.getValue());
        vao.updateVertexAttributePointer(attributeIndex, singleVertexAttributeLength, singleVertexSizeInBytes, singleVertexAttributeStartingOffsetSizeInBytes);
        unbind();
        vao.unbind();
    }

    public void unbind() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void clear() {
        glDeleteBuffers(id);
    }
}
