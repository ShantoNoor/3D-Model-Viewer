package com.modelviewer.Renderer;

import com.modelviewer.Renderer.Shader.ShaderProgram;
import com.modelviewer.Utils.Utils;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS;
import static org.lwjgl.stb.STBImage.*;

public class Texture {
    private int id;
    private String filepath;
    private int width, height;

    public boolean init(String filepath, boolean asRed, boolean flipY) {
        this.filepath = filepath;

        // Generate texture on GPU
        id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);

        // Set texture parameters
        // Repeat image in both directions
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        // When stretching the image, pixelate
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        // When shrinking an image, pixelate
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);
        stbi_set_flip_vertically_on_load(flipY);
        ByteBuffer image = stbi_load(filepath, width, height, channels, 0);

        if (image != null) {
            this.width = width.get(0);
            this.height = height.get(0);

            if (channels.get(0) == 3 && asRed) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, width.get(0), height.get(0),
                        0, GL_RGB, GL_UNSIGNED_BYTE, image);
            } else if (channels.get(0) == 4 && asRed) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, width.get(0), height.get(0),
                        0, GL_RGBA, GL_UNSIGNED_BYTE, image);
            } else if (channels.get(0) == 3) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width.get(0), height.get(0),
                        0, GL_RGB, GL_UNSIGNED_BYTE, image);
            } else if (channels.get(0) == 4) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0),
                        0, GL_RGBA, GL_UNSIGNED_BYTE, image);
            } else if (channels.get(0) == 1) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, width.get(0), height.get(0),
                            0, GL_RED, GL_UNSIGNED_BYTE, image);
            } else {
                System.out.println("Error: (Texture) Unknown number of channesl '" + channels.get(0) + "'");
                return false;
            }
        } else {
            System.out.println("Error: (Texture) Could not load image '" + filepath + "'");
            return false;
        }

        stbi_image_free(image);
        return true;
    }

    public boolean init(String filepath, boolean asRed) {
        return init(filepath, asRed, true);
    }

    public boolean init(String filepath) {
        return init(filepath, false, true);
    }

    public boolean initCubeMap(String folderPath) {
        String[] facesCubemap = new String[6];
        facesCubemap[0] = folderPath + "/right.jpg";
        facesCubemap[1] = folderPath + "/left.jpg";
        facesCubemap[2] = folderPath + "/top.jpg";
        facesCubemap[3] = folderPath + "/bottom.jpg";
        facesCubemap[4] = folderPath +  "/front.jpg";
        facesCubemap[5] = folderPath + "/back.jpg";

        id = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, id);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);

        for (int i = 0; i < 6; i++)
        {
            IntBuffer width = BufferUtils.createIntBuffer(1);
            IntBuffer height = BufferUtils.createIntBuffer(1);
            IntBuffer channels = BufferUtils.createIntBuffer(1);
            ByteBuffer data = stbi_load(facesCubemap[i], width, height, channels, 0);
            if (data != null)
            {
                stbi_set_flip_vertically_on_load(false);
                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB, width.get(0), height.get(0),
                        0, GL_RGB, GL_UNSIGNED_BYTE, data);
                stbi_image_free(data);
            }
            else
            {
                System.out.println("Failed to load texture: " + facesCubemap[i]);
                return false;
            }
        }
        return true;
    }

    public void safeBind(ShaderProgram shaderProgram, String shaderSamplerName, int textureSlot) {
        if(textureSlot >= Utils.queryNumberOfMaxTextureUnits()) {
            System.out.println("Texture slot number is greater than max texture units number of this system.");
            System.exit(-1);
        }

        shaderProgram.safeUpload(shaderSamplerName, textureSlot);
        glActiveTexture(GL_TEXTURE0 + textureSlot);
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void bind(ShaderProgram shaderProgram, String shaderSamplerName, int textureSlot) {
        if(textureSlot >= Utils.queryNumberOfMaxTextureUnits()) {
            System.out.println("Texture slot number is greater than max texture units number of this system.");
            System.exit(-1);
        }

        shaderProgram.upload(shaderSamplerName, textureSlot);
        glActiveTexture(GL_TEXTURE0 + textureSlot);
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void safeBindCubeMap(ShaderProgram shaderProgram, String shaderSamplerName, int textureSlot) {
        if(textureSlot >= Utils.queryNumberOfMaxTextureUnits()) {
            System.out.println("Texture slot number is greater than max texture units number of this system.");
            System.exit(-1);
        }

        shaderProgram.safeUpload(shaderSamplerName, textureSlot);
        glActiveTexture(GL_TEXTURE0 + textureSlot);
        glBindTexture(GL_TEXTURE_CUBE_MAP, id);
    }

    public void bindCubeMap(ShaderProgram shaderProgram, String shaderSamplerName, int textureSlot) {
        if(textureSlot >= Utils.queryNumberOfMaxTextureUnits()) {
            System.out.println("Texture slot number is greater than max texture units number of this system.");
            System.exit(-1);
        }

        shaderProgram.upload(shaderSamplerName, textureSlot);
        glActiveTexture(GL_TEXTURE0 + textureSlot);
        glBindTexture(GL_TEXTURE_CUBE_MAP, id);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public int getWidth() {
        return this.width;
    }

    public String getFilepath() {
        return this.filepath;
    }

    public int getHeight() {
        return this.height;
    }

    public int getId() {
        return id;
    }

    public void clear() {
        glDeleteTextures(id);
        filepath = null;
    }
 }
