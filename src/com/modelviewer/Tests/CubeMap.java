package com.modelviewer.Tests;

import com.modelviewer.Camera.Camera;
import com.modelviewer.Renderer.Cube;
import com.modelviewer.Renderer.Mesh.Model;
import com.modelviewer.Renderer.Quad;
import com.modelviewer.Renderer.Shader.ShaderProgram;
import com.modelviewer.Renderer.Texture;
import com.modelviewer.Utils.Constants;
import com.modelviewer.Window.Window;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS;
import static org.lwjgl.stb.STBImage.*;

public class CubeMap extends Window {
    private Camera camera;
    private ShaderProgram shaderProgram;
    private Model model;
    private Texture baseColor;
    private Texture normalMap;
    private Texture aoMap;
    private Texture metalnessMap;
    private Texture roughnessMap;

    public CubeMap(int width, int height, String title) {
        super(width, height, title);
    }

    Matrix4f rotate;

    private Quad quad;
    private Cube cube;
    private ShaderProgram quadShader;

    int cubemapTexture;

    @Override
    public void setup() {
        shaderProgram = new ShaderProgram("shaders/default.vs.glsl", "shaders/default.fs.glsl");
        shaderProgram.compile();
        shaderProgram.safeUpload("flipTexCordY", 1);

        normalMap = new Texture();
        normalMap.init("TestModels/backpack/normal.png");

        baseColor = new Texture();
        baseColor.init("TestModels/backpack/diffuse.jpg");

        aoMap = new Texture();
        aoMap.init("TestModels/backpack/ao.jpg", true);

        metalnessMap = new Texture();
        metalnessMap.init("TestModels/backpack/specular.jpg", true);

        roughnessMap = new Texture();
        roughnessMap.init("TestModels/backpack/roughness.jpg", true);

        model = new Model();
        model.loadMesh("TestModels/backpack/backpack.obj");

        camera = new Camera();
        camera.setPosition(new Vector3f(0.0f, 0.0f, model.getDistance()));

        rotate = new Matrix4f();

        quad = new Quad();
        cube = new Cube();
        quadShader = new ShaderProgram("shaders/cubeMap.vs.glsl", "shaders/cubeMap.fs.glsl");
        quadShader.compile();




        // All the faces of the cubemap (make sure they are in this exact order)
        String[] facesCubemap = new String[6];
        facesCubemap[0] = "CubeMaps/CubeMap1/right.jpg";
        facesCubemap[1] = "CubeMaps/CubeMap1/left.jpg";
        facesCubemap[2] = "CubeMaps/CubeMap1/top.jpg";
        facesCubemap[3] = "CubeMaps/CubeMap1/bottom.jpg";
        facesCubemap[4] =  "CubeMaps/CubeMap1/front.jpg";
        facesCubemap[5] = "CubeMaps/CubeMap1/back.jpg";

//        facesCubemap[0] = "CubeMaps/CubeMap2/right copy.jpg";
//        facesCubemap[1] = "CubeMaps/CubeMap2/left.jpg";
//        facesCubemap[2] = "CubeMaps/CubeMap2/top.jpg";
//        facesCubemap[3] = "CubeMaps/CubeMap2/bottom.jpg";
//        facesCubemap[4] =  "CubeMaps/CubeMap2/front.jpg";
//        facesCubemap[5] = "CubeMaps/CubeMap2/back.jpg";

        // Creates the cubemap texture object
        cubemapTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubemapTexture);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        // These are very important to prevent seams
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        // This might help with seams on some systems
        glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
//
        // Cycles through all the textures and attaches them to the cubemap object
        for (int i = 0; i < 6; i++)
        {
            IntBuffer width = BufferUtils.createIntBuffer(1);
            IntBuffer height = BufferUtils.createIntBuffer(1);
            IntBuffer channels = BufferUtils.createIntBuffer(1);
//            unsigned char* data = stbi_load(facesCubemap[i].c_str(), &width, &height, &nrChannels, 0);
            ByteBuffer data = stbi_load(facesCubemap[i], width, height, channels, 0);
            if (data != null)
            {
                stbi_set_flip_vertically_on_load(false);
//                glTexImage2D
//                        (
//                                GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
//                                0,
//                                GL_RGB,
//                                width,
//                                height,
//                                0,
//                                GL_RGB,
//                                GL_UNSIGNED_BYTE,
//                                data
//                        );
                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB, width.get(0), height.get(0),
                        0, GL_RGB, GL_UNSIGNED_BYTE, data);
                stbi_image_free(data);
            }
            else
            {
                System.out.println("Failed to load texture: " + facesCubemap[i]);
                System.exit(-1);
            }
        }
    }

    @Override
    public void loop(float dt) {
        camera.mouseUpdate(glfwWindow, width, height);
        camera.keyboardUpdate(dt);

        shaderProgram.safeUpload("projection", projectionMatrix);

        shaderProgram.safeUpload("view", camera.getViewMatrix());
        shaderProgram.safeUpload("camPos", camera.getPosition());
        shaderProgram.safeUpload("lightF", new Vector3f(0.0f, 1.0f, -1.0f));
        shaderProgram.safeUpload("rotate", rotate.rotate(dt, Constants.Y_AXIS));

        baseColor.bind(shaderProgram, "baseColor", 0);
        normalMap.bind(shaderProgram, "normalMap", 1);
        aoMap.bind(shaderProgram, "aoMap", 2);
        metalnessMap.bind(shaderProgram, "metalnessMap", 3);
        roughnessMap.bind(shaderProgram, "roughnessMap", 4);

        model.rotate(dt, Constants.Y_AXIS);
        model.render(shaderProgram);

        glDepthFunc(GL_LEQUAL);
        quadShader.bind();
        quadShader.upload("env", 0);
        quadShader.upload("view", camera.getViewMatrix());
        quadShader.upload("projection", getProjectionMatrix());
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubemapTexture);
//        quad.render(quadShader);
        cube.render(quadShader);
        glDepthFunc(GL_LESS);
    }

    public static void main(String[] args) {
        new CubeMap(1200, 800, "Cube Map").run();
    }
}
