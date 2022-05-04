package com.modelviewer.Renderer;
import com.modelviewer.Renderer.Shader.ShaderProgram;
import com.modelviewer.Utils.Utils;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Quad {
    private float val = 1.0f;
    private float[] vertexArray = {
             // positions
             val, -val, 0.0f,
            -val,  val, 0.0f,
             val,  val, 0.0f,
            -val, -val, 0.0f
    };
    private int[] elementArray = {
            2, 1, 0,
            0, 1, 3
    };

    private int vaoID, vboID, eboID;

    public Quad() {
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vaoID);
        glBufferData(GL_ARRAY_BUFFER, Utils.convertFloatArrayToFloatBuffer(vertexArray), GL_STATIC_DRAW);

        eboID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, Utils.convertIntArrayToIntBuffer(elementArray), GL_STATIC_DRAW);

        int positionSize = 3;
        int vertexSize = positionSize * 4;

        glVertexAttribPointer(0, positionSize, GL_FLOAT, false, vertexSize, 0);
        glEnableVertexAttribArray(0);
    }

    public void render(ShaderProgram shaderProgram) {
        shaderProgram.bind();
        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        shaderProgram.unbind();
    }
}
