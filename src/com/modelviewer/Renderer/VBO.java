package com.modelviewer.Renderer;

import com.modelviewer.Utils.Utils;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL15.*;

public class VBO extends OpenGLObject {
    public VBO() {
        id = glGenBuffers();
    }

    public void bind() {
        glBindBuffer(GL_ARRAY_BUFFER, id);
    }

    public void uploadVertexAttributeData(VAO vao, ArrayList<Float> data, int attributeIndex, int singleVertexAttributeSize,
                                          int vertexAttributeStartOffset, int nextVertexAttributeOffsetSize, BufferDataType bufferDataType) {
        vao.bind();
        bind();
        glBufferData(GL_ARRAY_BUFFER, Utils.convertFloatArrayListToFloatBuffer(data), bufferDataType.getValue());
        vao.updateVertexAttributePointer(attributeIndex, singleVertexAttributeSize, vertexAttributeStartOffset, nextVertexAttributeOffsetSize);
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
