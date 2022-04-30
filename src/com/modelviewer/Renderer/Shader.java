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
    private String filepath;

    public Shader(String filepath) {
        this.filepath = filepath;
        try {
            String source = new String(Files.readAllBytes(Paths.get(filepath)));
            String[] splitString = source.split("(#type)( )+([a-zA-Z]+)");

            int index = source.indexOf("#type") + 6;
            int endOfLine = source.indexOf("\n", index);
            String firstPattern = source.substring(index, endOfLine).trim();

            index = source.indexOf("#type", endOfLine) + 6;
            endOfLine = source.indexOf("\n", index);
            String secondPattern = source.substring(index, endOfLine).trim();

            if(firstPattern.equals("vertex")) {
                vertexSource = splitString[1];
            } else if(firstPattern.equals("fragment")) {
                fragmentSource = splitString[1];
            } else {
                throw new IOException("Unexpected token " + firstPattern + " in " + filepath);
            }

            if(secondPattern.equals("vertex")) {
                vertexSource = splitString[2];
            } else if(secondPattern.equals("fragment")) {
                fragmentSource = splitString[2];
            } else {
                throw new IOException("Unexpected token " + secondPattern + " in " + filepath);
            }

        } catch (IOException e) {
            e.printStackTrace();
            assert false : "Error unable to open file for shader: " + filepath;
        }

        compile();
    }

    public void compile() {
        int vertexID, fragmentID;
        vertexID = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexID, vertexSource);
        glCompileShader(vertexID);

        int success = glGetShaderi(vertexID, GL_COMPILE_STATUS);
        if(success == GL_FALSE) {
            int len = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH);
            System.out.println("Vertex Shader compilation failed: " + filepath);
            System.out.println(glGetShaderInfoLog(vertexID, len));
            assert false : "";
        }

        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentID, fragmentSource);
        glCompileShader(fragmentID);

        success = glGetShaderi(fragmentID, GL_COMPILE_STATUS);
        if(success == GL_FALSE) {
            int len = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH);
            System.out.println("Fragment Shader compilation failed: " + filepath);
            System.out.println(glGetShaderInfoLog(fragmentID, len));
            assert false : "";
        }

        shaderProgramID = glCreateProgram();
        glAttachShader(shaderProgramID, vertexID);
        glAttachShader(shaderProgramID, fragmentID);
        glLinkProgram(shaderProgramID);

        success = glGetProgrami(shaderProgramID, GL_LINK_STATUS);
        if(success == GL_FALSE) {
            int len = glGetProgrami(shaderProgramID, GL_INFO_LOG_LENGTH);
            System.out.println("Shader program linking failed: " + filepath);
            System.out.println(glGetProgramInfoLog(shaderProgramID, len));
            assert false : "";
        }
    }

    public void use() {
        glUseProgram(shaderProgramID);
    }

    public void detach() {
        glUseProgram(0);
    }

    public void upload(String varName, Matrix4f mat4) {
        use();
        uploadN(varName, mat4);
        detach();
    }

    public void uploadN(String varName, Matrix4f mat4) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        mat4.get(buffer);
        glUniformMatrix4fv(varLocation, false, buffer);
//        mat4.identity();
    }

    public void upload(String varName, Vector3f vec3) {
        use();
        uploadN(varName, vec3);
        detach();
    }

    public void uploadN(String varName, Vector3f vec3) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
        vec3.get(buffer);
        glUniform3fv(varLocation, buffer);
    }

    public void upload(String varName, float value) {
        use();
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        glUniform1f(varLocation, value);
        detach();
    }
}

