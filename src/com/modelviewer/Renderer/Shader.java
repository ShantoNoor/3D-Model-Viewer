package com.modelviewer.Renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUniform3fv;

public class Shader {
    private int shaderProgramID;

    private String vertexSource;
    private String fragmentSource;
    private String vertexSourcePath;
    private String fragmentSourcePath;

    public Shader(String vertexSourcePath, String fragmentSourcePath) {
        this.vertexSourcePath = vertexSourcePath;
        this.fragmentSourcePath = fragmentSourcePath;

        if(vertexSourcePath == null) {
            System.out.println("Vertex Source Path is null.");
            System.exit(-1);
        }

        if(fragmentSourcePath == null) {
            System.out.println("Fragment Source Path is null.");
            System.exit(-1);
        }

        this.vertexSource = read(vertexSourcePath);
        this.fragmentSource = read(fragmentSourcePath);
    }

    public String read(String filePath) {
        String source = null;

        try {
            source = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            System.out.println("Error unable to open Shader file: " + filePath);
            e.printStackTrace();
            System.exit(-1);
        }

        return source;
    }

    public void compile() {
        if(vertexSource == null) {
            System.out.println("Vertex Source is null.");
            System.exit(-1);
        }

        if(fragmentSource == null) {
            System.out.println("Fragment Source is null.");
            System.exit(-1);
        }

        int vertexID, fragmentID;
        vertexID = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexID, vertexSource);
        glCompileShader(vertexID);

        int success = glGetShaderi(vertexID, GL_COMPILE_STATUS);
        if(success == GL_FALSE) {
            int len = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH);
            System.out.println("Vertex Shader compilation failed: " + vertexSourcePath);
            System.out.println(glGetShaderInfoLog(vertexID, len));
            System.exit(-1);
        }

        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentID, fragmentSource);
        glCompileShader(fragmentID);

        success = glGetShaderi(fragmentID, GL_COMPILE_STATUS);
        if(success == GL_FALSE) {
            int len = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH);
            System.out.println("Fragment Shader compilation failed: " + fragmentSourcePath);
            System.out.println(glGetShaderInfoLog(fragmentID, len));
            System.exit(-1);
        }

        shaderProgramID = glCreateProgram();
        glAttachShader(shaderProgramID, vertexID);
        glAttachShader(shaderProgramID, fragmentID);
        glLinkProgram(shaderProgramID);

        success = glGetProgrami(shaderProgramID, GL_LINK_STATUS);
        if(success == GL_FALSE) {
            int len = glGetProgrami(shaderProgramID, GL_INFO_LOG_LENGTH);
            System.out.println("Shader program linking failed: " + vertexSourcePath + " " + fragmentSourcePath);
            System.out.println(glGetProgramInfoLog(shaderProgramID, len));
            System.exit(-1);
        }
    }

    public void activate() {
        glUseProgram(shaderProgramID);
    }

    public void deactivate() {
        glUseProgram(0);
    }

    public void upload(String varName, Matrix4f mat4) {
        activate();
        uploadN(varName, mat4);
        deactivate();
    }

    public void uploadN(String varName, Matrix4f mat4) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        mat4.get(buffer);
        glUniformMatrix4fv(varLocation, false, buffer);
    }

    public void upload(String varName, Vector3f vec3) {
        activate();
        uploadN(varName, vec3);
        deactivate();
    }

    public void uploadN(String varName, Vector3f vec3) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
        vec3.get(buffer);
        glUniform3fv(varLocation, buffer);
    }

    public void upload(String varName, float value) {
        activate();
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        glUniform1f(varLocation, value);
        deactivate();
    }
}

