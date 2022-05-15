package com.modelviewer.Renderer.Shader;

import com.modelviewer.Window.Input.FileReader;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public class Shader {
    public String path;
    public String code;
    public ShaderType type;
    public int id;

    public Shader(String path, ShaderType type) {
        this.path = path;
        this.type = type;
        this.code = FileReader.read(path);
    }
    
    public void compile() {
        if (code == null) {
            System.out.println(type.toString() + " Code is null.");
            System.out.println("Shader Code Path: " + path);
            System.exit(-1);
        }

        id = glCreateShader(type.getValue());
        glShaderSource(id, code);
        glCompileShader(id);

        int success = glGetShaderi(id, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int infoLength = glGetShaderi(id, GL_INFO_LOG_LENGTH);
            System.out.println(type.toString() + " Shader compilation failed: " + path);
            System.out.println(glGetShaderInfoLog(id, infoLength));
            System.exit(-1);
        }
    }

    public void clear() {
        path = null;
        code = null;
        type = null;
        glDeleteShader(id);
    }
}
