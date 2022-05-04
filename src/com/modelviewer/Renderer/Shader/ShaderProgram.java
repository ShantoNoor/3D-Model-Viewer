package com.modelviewer.Renderer.Shader;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUniform3fv;

public class ShaderProgram {
    private int id;
    private ArrayList<Shader> shaders = new ArrayList<>();

    public ShaderProgram() { }

    public ShaderProgram(String vertexShaderCode, String fragmentShaderCode) {
        addShader(vertexShaderCode, ShaderType.VERTEX);
        addShader(fragmentShaderCode, ShaderType.FRAGMENT);
    }

    public void addShader(String shaderCodePath, ShaderType shaderType) {
        shaders.add(new Shader(shaderCodePath, shaderType));
    }

    public void compile() {
        id = glCreateProgram();

        for(int i = 0; i < shaders.size(); ++i) {
            Shader shader = shaders.get(i);
            shader.compile();
            glAttachShader(id, shader.id);
        }

        link();
    }

    public void link() {
        glLinkProgram(id);

        int success = glGetProgrami(id, GL_LINK_STATUS);
        if(success == GL_FALSE) {
            int infoLength = glGetProgrami(id, GL_INFO_LOG_LENGTH);

            System.out.println("Shader program linking failed: ");
            for(int i = 0; i < shaders.size(); ++i) {
                System.out.println(shaders.get(i).path);
            }

            System.out.println(glGetProgramInfoLog(id, infoLength));
            System.exit(-1);
        }

        for(int i = 0; i < shaders.size(); ++i) {
            glDetachShader(id, shaders.get(i).id);
            shaders.get(i).clear();
        }

        shaders.clear();
    }

    public void bind() {
        glUseProgram(id);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void safeUpload(String varName, Matrix4f mat4) {
        bind();
        upload(varName, mat4);
        unbind();
    }

    public void upload(String varName, Matrix4f mat4) {
        int varLocation = glGetUniformLocation(id, varName);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        mat4.get(buffer);
        glUniformMatrix4fv(varLocation, false, buffer);
    }

    public void safeUpload(String varName, Vector3f vec3) {
        bind();
        upload(varName, vec3);
        unbind();
    }

    public void upload(String varName, Vector3f vec3) {
        int varLocation = glGetUniformLocation(id, varName);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
        vec3.get(buffer);
        glUniform3fv(varLocation, buffer);
    }

    public void safeUpload(String varName, float value) {
        bind();
        int varLocation = glGetUniformLocation(id, varName);
        glUniform1f(varLocation, value);
        unbind();
    }

    public void upload(String varName, float value) {
        int varLocation = glGetUniformLocation(id, varName);
        glUniform1f(varLocation, value);
    }

    public void clear() {
        glDeleteProgram(id);
    }
}

