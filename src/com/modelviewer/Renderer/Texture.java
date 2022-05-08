package com.modelviewer.Renderer;

import com.modelviewer.Renderer.Shader.ShaderProgram;
import com.modelviewer.Utils.Utils;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.stb.STBImage.*;

public class Texture {
    private int id;
    private String filepath;
    private int width, height;

    public void init(String filepath, boolean asRed) {
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
        stbi_set_flip_vertically_on_load(true);
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
                return;
            }
        } else {
            System.out.println("Error: (Texture) Could not load image '" + filepath + "'");
            return;
        }

        stbi_image_free(image);
    }

    public void init(String filepath) {
        init(filepath, false);
    }

    public void bind(ShaderProgram shaderProgram, String shaderSamplerName, int textureSlot) {
        if(textureSlot >= Utils.queryNumberOfMaxTextureUnits()) {
            System.out.println("Texture slot number is greater than max texture units number of this system.");
            System.exit(-1);
        }

        shaderProgram.safeUpload(shaderSamplerName, textureSlot);
        glActiveTexture(GL_TEXTURE0 + textureSlot);
        glBindTexture(GL_TEXTURE_2D, id);
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
    }
 }
