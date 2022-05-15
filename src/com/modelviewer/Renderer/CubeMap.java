package com.modelviewer.Renderer;

import com.modelviewer.Renderer.Shader.ShaderProgram;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public class CubeMap {
    float skyboxVertices[] = {
            // positions
            -1.0f,  1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f, -1.0f,

            -1.0f, -1.0f,  1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f,  1.0f,
            -1.0f, -1.0f,  1.0f,

            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f,  1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,

            -1.0f, -1.0f,  1.0f,
            -1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f, -1.0f,  1.0f,
            -1.0f, -1.0f,  1.0f,

            -1.0f,  1.0f, -1.0f,
            1.0f,  1.0f, -1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            -1.0f,  1.0f,  1.0f,
            -1.0f,  1.0f, -1.0f,

            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f,  1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f,  1.0f,
            1.0f, -1.0f,  1.0f
    };

    private VAO vao;
    private VBO vbo;

    private ArrayList<Texture> cubeMaps;
    public int activeCubeMapIndex = 0;

    public CubeMap() {
        vao = new VAO();
        vbo = new VBO();
        vbo.uploadVertexAttributeData(vao, skyboxVertices, 0, 3, BufferDataType.STATIC);
        cubeMaps = new ArrayList<>();
    }

    public void render(ShaderProgram shaderProgram) {
        glDepthFunc(GL_LEQUAL);
        shaderProgram.bind();
        cubeMaps.get(activeCubeMapIndex).bindCubeMap(shaderProgram, "env", 6);
        vao.bind();
        vbo.bind();
        glDrawArrays(GL_TRIANGLES, 0, 36);
        vbo.unbind();
        vao.unbind();
        shaderProgram.unbind();
        glDepthFunc(GL_LESS);
    }

    public void clear() {
        vao.clear();
        vbo.clear();
        vao = null;
        vbo = null;
        skyboxVertices = null;

        for(int i = 0; i < cubeMaps.size(); ++i) {
            cubeMaps.get(i).clear();
        }

        cubeMaps = null;
    }

    public boolean addCubeMap(String folderPath) {
        boolean ret1 = false;
        boolean ret2 = false;
        Texture cubeMap = new Texture();
        ret1 = cubeMap.initCubeMap(folderPath);
        ret2 = cubeMaps.add(cubeMap);
        return ret1 && ret2;
    }

    public Texture getCubeMaps(int index) {
        return cubeMaps.get(index);
    }
}
