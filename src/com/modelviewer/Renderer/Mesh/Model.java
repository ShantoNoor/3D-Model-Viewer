package com.modelviewer.Renderer.Mesh;

import com.modelviewer.Renderer.*;
import com.modelviewer.Renderer.Shader.ShaderProgram;
import com.modelviewer.Utils.Constants;
import com.modelviewer.Utils.Utils;
import com.modelviewer.Window.NuklearLayer.NuklearCheckbox;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.nuklear.NkContext;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL32.glDrawElementsBaseVertex;

public class Model {
    private MeshInfo[] meshes;
    private Material[] materials;

    private IntBuffer indices;
    private FloatBuffer positions, normals, texCords, colors, tangents, bitangents;

    private static final int POSITION_BUFFER = 0;
    private static final int TEX_COORD_BUFFER = 1;
    private static final int NORMAL_BUFFER = 2;
    private static final int COLOR_BUFFER = 3;
    private static final int TANGENT_BUFFER = 4;
    private static final int BITANGENT_BUFFER = 5;
    private static final int NUMBER_OF_BUFFER = 6;

    private int haveTangents, haveColors;

    private VAO vao;
    private VBO[] vbos;
    private IBO ibo;

    private int numberOfVertices = 0, numberOfIndices = 0;
    private boolean modelSuccessfullyLoaded = false;

    private boolean enableRotation = false;
    private Vector3f axisOfRotation = Constants.Y_AXIS;
    private int rotationAxis = 1;

    private Vector3f min = new Vector3f(Float.MAX_VALUE);
    private Vector3f max = new Vector3f(Float.MIN_VALUE);
    private Vector3f origin = new Vector3f(0.0f);
    private float distance;
    private float minDistance = 15.0f;
    private float maxDistance = 30.0f;

    private Matrix4f transform = new Matrix4f().identity();

    private NkContext ctx;

    private NuklearCheckbox flipX, flipY;

    public Model(NkContext ctx) {
        this.ctx = ctx;
        this.flipX = new NuklearCheckbox(ctx, "FlipX");
        this.flipY = new NuklearCheckbox(ctx, "FlipY");
        this.vao = new VAO();
        this.vbos = new VBO[NUMBER_OF_BUFFER];
        this.ibo = new IBO();
    }

    public boolean loadMesh(String filePath) {
        if(filePath == null) return false;

        if(modelSuccessfullyLoaded) {
            tempClear();
            vao = new VAO();
            vbos = new VBO[NUMBER_OF_BUFFER];
            ibo = new IBO();
        }

        int importFlags = Assimp.aiProcess_CalcTangentSpace
                            | Assimp.aiProcess_GenSmoothNormals
                            | Assimp.aiProcess_JoinIdenticalVertices
                            | Assimp.aiProcess_ImproveCacheLocality
                            | Assimp.aiProcess_LimitBoneWeights
                            | Assimp.aiProcess_RemoveRedundantMaterials
                            | Assimp.aiProcess_SplitLargeMeshes
                            | Assimp.aiProcess_Triangulate
                            | Assimp.aiProcess_GenUVCoords
                            | Assimp.aiProcess_SortByPType
                            | Assimp.aiProcess_GenBoundingBoxes
                            ;

        AIScene pScene = Assimp.aiImportFile(filePath, importFlags);

        if(pScene != null) {
            modelSuccessfullyLoaded = initFromScene(pScene, filePath);
        } else {
            modelSuccessfullyLoaded = false;
            System.out.println("Error parsing: " + filePath + " " + Assimp.aiGetErrorString().toString());
            System.exit(-1);
        }

        processMeshTransform(pScene.mRootNode(), new Matrix4f());
        adjustTransform();
        initAllMaterials(pScene);

        return modelSuccessfullyLoaded;
    }

    private void initAllMaterials(AIScene scene) {
        PointerBuffer materials = scene.mMaterials();
        for (int i = 0; i < scene.mNumMaterials(); ++i) {
            AIMaterial material = AIMaterial.create(materials.get(i));

            PointerBuffer materialProperties = material.mProperties();
            this.materials[i] = new Material(ctx);

            for (int j = 0; j < material.mNumProperties(); ++j) {
                AIMaterialProperty property = AIMaterialProperty.create(materialProperties.get(j));
                String key = property.mKey().dataString();

                if(key.equals(Assimp.AI_MATKEY_NAME))
                {
                    this.materials[i].materialName = Utils.convertByteBufferToString(property.mData());
                }
                else if(key.equals(Assimp.AI_MATKEY_GLOSSINESS_FACTOR))
                {
//                    System.out.println("GLOSSINESS_FACTOR: " + property.mData().getFloat());
                    this.materials[i].roughnessFactor.set(1.0f - property.mData().getFloat());
                }
                else if(key.equals(Assimp.AI_MATKEY_REFLECTIVITY))
                {
//                    System.out.println("reflectivity: " + property.mData().getFloat());
                    this.materials[i].reflectivity.set(property.mData().getFloat());
                }
                else if(key.equals(Assimp.AI_MATKEY_METALLIC_FACTOR))
                {
//                    System.out.println("metallicFactor: " + property.mData().getFloat());
                    this.materials[i].metallicFactor.set(property.mData().getFloat());
                }
                else if(key.equals(Assimp.AI_MATKEY_EMISSIVE_INTENSITY))
                {
//                    System.out.println("emissiveIntensity: " + property.mData().getFloat());
                    this.materials[i].emissiveIntensity.set(property.mData().getFloat());
                }
                else if(key.equals(Assimp.AI_MATKEY_ROUGHNESS_FACTOR))
                {
//                    System.out.println("roughnessFactor: " + property.mData().getFloat());
                    this.materials[i].roughnessFactor.set(property.mData().getFloat());
                }
                else if(key.equals(Assimp.AI_MATKEY_SHININESS))
                {
//                    System.out.println("shininess: " + property.mData().getFloat());
                    this.materials[i].shininess.set(property.mData().getFloat());
                }
                else if(key.equals(Assimp.AI_MATKEY_SHININESS_STRENGTH))
                {
//                    System.out.println("shininessIntensity: " + property.mData().getFloat());
                    this.materials[i].shininessIntensity.set(property.mData().getFloat());
                }
                else if(key.equals(Assimp.AI_MATKEY_SPECULAR_FACTOR))
                {
//                    System.out.println("reflectivity: " + property.mData().getFloat());
                    this.materials[i].reflectivity.set(property.mData().getFloat());
                }
                else if(key.equals(Assimp.AI_MATKEY_COLOR_SPECULAR))
                {
                    AIColor4D color4D = AIColor4D.create();
                    Assimp.aiGetMaterialColor(material, Assimp.AI_MATKEY_COLOR_SPECULAR, 0, 0, color4D);
                    this.materials[i].specularColor.set(Utils.convertAIColor4DToVector4f(color4D));
//                    Utils.printVector4f(this.materials[i].specularColor.get(), "specular");
                }
                else if(key.equals(Assimp.AI_MATKEY_COLOR_REFLECTIVE))
                {
                    AIColor4D color4D = AIColor4D.create();
                    Assimp.aiGetMaterialColor(material, Assimp.AI_MATKEY_COLOR_REFLECTIVE, 0, 0, color4D);
                    this.materials[i].specularColor.set(Utils.convertAIColor4DToVector4f(color4D));
//                    Utils.printVector4f(this.materials[i].specularColor.get(), "specular");
                }
                else if(key.equals(Assimp.AI_MATKEY_COLOR_AMBIENT))
                {
                    AIColor4D color4D = AIColor4D.create();
                    Assimp.aiGetMaterialColor(material, Assimp.AI_MATKEY_COLOR_AMBIENT, 0, 0, color4D);
                    this.materials[i].ambientColor.set(Utils.convertAIColor4DToVector4f(color4D));
//                    Utils.printVector4f(this.materials[i].ambientColor.get(), "ambient");
                }
                else if(key.equals(Assimp.AI_MATKEY_COLOR_DIFFUSE))
                {
                    AIColor4D color4D = AIColor4D.create();
                    Assimp.aiGetMaterialColor(material, Assimp.AI_MATKEY_COLOR_DIFFUSE, 0, 0, color4D);
                    this.materials[i].diffuseColor.set(Utils.convertAIColor4DToVector4f(color4D));
//                    Utils.printVector4f(this.materials[i].diffuseColor.get(), "diffuse");
                }
                else if(key.equals(Assimp.AI_MATKEY_BASE_COLOR))
                {
                    AIColor4D color4D = AIColor4D.create();
                    Assimp.aiGetMaterialColor(material, Assimp.AI_MATKEY_BASE_COLOR, 0, 0, color4D);
                    this.materials[i].diffuseColor.set(Utils.convertAIColor4DToVector4f(color4D));
//                    Utils.printVector4f(this.materials[i].diffuseColor.get(), "diffuse");
                }
                else if(key.equals(Assimp.AI_MATKEY_COLOR_EMISSIVE))
                {
                    AIColor4D color4D = AIColor4D.create();
                    Assimp.aiGetMaterialColor(material, Assimp.AI_MATKEY_COLOR_EMISSIVE, 0, 0, color4D);
                    this.materials[i].emissiveColor.set(Utils.convertAIColor4DToVector4f(color4D));
//                    Utils.printVector4f(this.materials[i].emissiveColor.get(), "emissive");
                }
            }
            this.materials[i].setNames(i);
        }
    }

    private void adjustTransform() {
        distance = Vector3f.distance(max.x, max.y, max.z, min.x, min.y, min.z);

        if(distance > maxDistance || distance < minDistance) {
            new Matrix4f().scale(maxDistance / distance).mul(transform, transform);

            max = Utils.mulVector3fWithMatrix4f(max, transform);
            min = Utils.mulVector3fWithMatrix4f(min, transform);

            distance = Vector3f.distance(max.x, max.y, max.z, min.x, min.y, min.z);
        }

        origin = max.add(min, origin).mul(0.5f, origin);

        Matrix4f translateToOrigin = new Matrix4f().translate(origin.mul(-1.0f, new Vector3f()));

        translateToOrigin.mul(transform, transform);

        origin = Utils.mulVector3fWithMatrix4f(origin, translateToOrigin);
    }

    private void processMeshTransform(AINode node, Matrix4f pTransform) {
        IntBuffer buffer = node.mMeshes();
        Matrix4f nTransform = Utils.convertAIMatrix4x4ToMatrix4f(node.mTransformation());
        nTransform.mul(pTransform, nTransform);
        for(int i = 0; i < node.mNumMeshes(); ++i) {
            int meshIndex = buffer.get(i);
            meshes[meshIndex].modelTransform = nTransform;
            processMinMax(meshIndex, nTransform);
        }

        for(int i = 0; i < node.mNumChildren(); ++i) {
            processMeshTransform(AINode.create(node.mChildren().get(i)), nTransform);
        }
    }

    private void processMinMax(int meshIndex, Matrix4f modelTransform) {
        Vector4f temp_min = new Vector4f(meshes[meshIndex].min, 1.0f);
        Vector4f temp_max = new Vector4f(meshes[meshIndex].max, 1.0f);

        temp_min.mul(modelTransform, temp_min);
        temp_max.mul(modelTransform, temp_max);

        meshes[meshIndex].min.x = temp_min.x;
        meshes[meshIndex].min.y = temp_min.y;
        meshes[meshIndex].min.z = temp_min.z;

        meshes[meshIndex].max.x = temp_max.x;
        meshes[meshIndex].max.y = temp_max.y;
        meshes[meshIndex].max.z = temp_max.z;

        if(min.x > temp_min.x) min.x = temp_min.x;
        if(min.y > temp_min.y) min.y = temp_min.y;
        if(min.z > temp_min.z) min.z = temp_min.z;

        if(max.x < temp_max.x) max.x = temp_max.x;
        if(max.y < temp_max.y) max.y = temp_max.y;
        if(max.z < temp_max.z) max.z = temp_max.z;
    }

    public void render(ShaderProgram shaderProgram) {
        shaderProgram.bind();
        vao.bind();
        ibo.bind();

        shaderProgram.upload("transform", transform);
        shaderProgram.upload("haveTangents", haveTangents);
        shaderProgram.upload("flipTexCordX", flipX.get());
        shaderProgram.upload("flipTexCordY", flipY.get());

        if(modelSuccessfullyLoaded) {
            for (int i = 0; i < meshes.length; ++i) {
                materials[meshes[i].materialIndex].upload(shaderProgram);
                shaderProgram.upload("mesh", meshes[i].modelTransform);
                glDrawElementsBaseVertex(GL_TRIANGLES, meshes[i].numberOfIndices, GL_UNSIGNED_INT, meshes[i].baseIndex * 4, meshes[i].baseVertex);
            }
        }

        ibo.unbind();
        vao.unbind();
        shaderProgram.unbind();
    }

    private boolean initFromScene(AIScene pScene, String filePath) {
        meshes = new MeshInfo[pScene.mNumMeshes()];
        materials = new Material[pScene.mNumMaterials()];

        countVerticesAndIndices(pScene);
        reserveSpace();
        initAllMeshes(pScene);

        loadBuffers();

        return true;
    }

    private void countVerticesAndIndices(AIScene pScene) {
        PointerBuffer buffer = pScene.mMeshes();

        for (int i = 0 ; i < meshes.length; i++) {
            AIMesh mesh = AIMesh.create(buffer.get(i));

            meshes[i] = new MeshInfo();
            meshes[i].materialIndex = mesh.mMaterialIndex();
            meshes[i].numberOfIndices = mesh.mNumFaces() * 3;
            meshes[i].numberOfVertices = mesh.mNumVertices();

            if(i > 0) {
                meshes[i].baseVertex = numberOfVertices;
                meshes[i].baseIndex = numberOfIndices;
            }

            numberOfIndices += meshes[i].numberOfIndices;
            numberOfVertices += meshes[i].numberOfVertices;
        }
    }

    private void reserveSpace() {
        positions = BufferUtils.createFloatBuffer(numberOfVertices * 3);
        colors = BufferUtils.createFloatBuffer(numberOfVertices * 4);
        normals = BufferUtils.createFloatBuffer(numberOfVertices * 3);
        texCords = BufferUtils.createFloatBuffer(numberOfVertices * 2);
        indices = BufferUtils.createIntBuffer(numberOfIndices * 3);
        tangents = BufferUtils.createFloatBuffer(numberOfVertices * 3);
        bitangents = BufferUtils.createFloatBuffer(numberOfVertices * 3);
    }

    private void initAllMeshes(AIScene pScene) {
        PointerBuffer buffer = pScene.mMeshes();

        for (int j = 0 ; j < meshes.length ; j++) {
            AIMesh mesh = AIMesh.create(buffer.get(j));

            meshes[j].materialIndex = mesh.mMaterialIndex();

            AIVector3D.Buffer vertexPositions = mesh.mVertices();
            for(int i = 0; i < vertexPositions.limit(); ++i) {
                AIVector3D vertexPosition = vertexPositions.get(i);

                positions.put(vertexPosition.x());
                positions.put(vertexPosition.y());
                positions.put(vertexPosition.z());
            }

            AIVector3D.Buffer vertexTexCoords = mesh.mTextureCoords(0);
            if(vertexTexCoords != null)
                for(int i = 0; i < vertexTexCoords.limit(); ++i) {
                    AIVector3D vertexTexCoord = vertexTexCoords.get(i);

                    texCords.put(vertexTexCoord.x());
                    texCords.put(vertexTexCoord.y());
                }

            AIVector3D.Buffer vertexNormals = mesh.mNormals();
            if(vertexNormals != null)
                for(int i = 0; i < vertexNormals.limit(); i++) {
                    AIVector3D vertexNormal = vertexNormals.get(i);

                    normals.put(vertexNormal.x());
                    normals.put(vertexNormal.y());
                    normals.put(vertexNormal.z());
                }

            AIColor4D.Buffer vertexColours = mesh.mColors(0);
            if(vertexColours != null) {
                haveColors = 1;
                for (int i = 0; i < vertexColours.limit(); i++) {
                    AIColor4D vertexColor = vertexColours.get(i);

                    colors.put(vertexColor.r());
                    colors.put(vertexColor.g());
                    colors.put(vertexColor.b());
                    colors.put(vertexColor.a());
                }
            } else {
                haveColors = 0;
            }

            for(int i = 0; i < mesh.mNumFaces(); i++) {
                AIFace face = mesh.mFaces().get(i);

                indices.put(face.mIndices().get(0));
                indices.put(face.mIndices().get(1));
                indices.put(face.mIndices().get(2));
            }

            // Setup bounding box
            AIAABB boundingBox = mesh.mAABB();

            meshes[j].min.x = boundingBox.mMin().x();
            meshes[j].min.y = boundingBox.mMin().y();
            meshes[j].min.z = boundingBox.mMin().z();

            meshes[j].max.x = boundingBox.mMax().x();
            meshes[j].max.y = boundingBox.mMax().y();
            meshes[j].max.z = boundingBox.mMax().z();

            AIVector3D.Buffer vertexTangents = mesh.mTangents();
            if(vertexTangents != null) {
                haveTangents = 1;

                for (int i = 0; i < vertexTangents.limit(); i++) {
                    AIVector3D vertexTangent = vertexTangents.get(i);
                    tangents.put(vertexTangent.x());
                    tangents.put(vertexTangent.y());
                    tangents.put(vertexTangent.z());
                }
            } else {
                haveTangents = 0;
            }

            AIVector3D.Buffer vertexBitangents = mesh.mBitangents();
            if(vertexBitangents != null) {
                haveTangents = 1;

                for (int i = 0; i < vertexBitangents.limit(); i++) {
                    AIVector3D vertexBitangent = vertexBitangents.get(i);
                    bitangents.put(vertexBitangent.x());
                    bitangents.put(vertexBitangent.y());
                    bitangents.put(vertexBitangent.z());
                }
            } else {
                haveTangents = 0;
            }
        }
    }

    private void loadBuffers() {
        vao.bind();
        for(int i = 0; i < vbos.length; ++i) {
            vbos[i] = new VBO();
        }

        vbos[POSITION_BUFFER].uploadVertexAttributeData(vao, positions.flip(), POSITION_BUFFER, 3, BufferDataType.STATIC);
        vbos[TEX_COORD_BUFFER].uploadVertexAttributeData(vao, texCords.flip(), TEX_COORD_BUFFER, 2, BufferDataType.STATIC);
        vbos[NORMAL_BUFFER].uploadVertexAttributeData(vao, normals.flip(), NORMAL_BUFFER, 3, BufferDataType.STATIC);

        if(haveColors > 0) {
            vbos[COLOR_BUFFER].uploadVertexAttributeData(vao, colors.flip(), COLOR_BUFFER, 4, BufferDataType.STATIC);
        }

        if(haveTangents > 0) {
            vbos[TANGENT_BUFFER].uploadVertexAttributeData(vao, tangents.flip(), TANGENT_BUFFER, 3, BufferDataType.STATIC);
            vbos[BITANGENT_BUFFER].uploadVertexAttributeData(vao, bitangents.flip(), BITANGENT_BUFFER, 3, BufferDataType.STATIC);
        }

        ibo.uploadIndicesData(vao, indices.flip(), BufferDataType.STATIC);
        vao.unbind();
    }

    private void tempClear() {
        positions.clear();
        texCords.clear();
        normals.clear();
        tangents.clear();
        bitangents.clear();
        colors.clear();
        indices.clear();

        positions = null;
        texCords = null;
        normals = null;
        tangents = null;
        bitangents = null;
        colors = null;
        indices = null;

        vao.clear();
        vao = null;

        ibo.clear();
        ibo = null;

        for(int i = 0; i < vbos.length; ++i) {
            vbos[i].clear();
            vbos[i] = null;
        }

        numberOfVertices = 0;
        numberOfIndices = 0;

        haveTangents = 0;
        haveColors = 0;

        modelSuccessfullyLoaded = false;
        enableRotation = false;
        axisOfRotation = Constants.Y_AXIS;
        rotationAxis = 1;

        min = new Vector3f(Float.MAX_VALUE);
        max = new Vector3f(Float.MIN_VALUE);
        origin = new Vector3f(0.0f);
        minDistance = 15.0f;
        maxDistance = 30.0f;

        transform = new Matrix4f().identity();

        flipX.set(0);
        flipY.set(0);

        for(int i = 0; i < materials.length; ++i) {
            materials[i].clear();
            materials[i] = null;
        }
        materials = null;

        for(int i = 0; i < meshes.length; ++i) {
            meshes[i].clear();
            meshes[i] = null;
        }
        meshes = null;
    }

    public void clear() {
        positions.clear();
        texCords.clear();
        normals.clear();
        tangents.clear();
        bitangents.clear();
        colors.clear();
        indices.clear();

        positions = null;
        texCords = null;
        normals = null;
        tangents = null;
        bitangents = null;
        colors = null;
        indices = null;

        vao.clear();
        vao = null;

        ibo.clear();
        ibo = null;

        for(int i = 0; i < vbos.length; ++i) {
            vbos[i].clear();
            vbos[i] = null;
        }


        axisOfRotation = null;
        ctx = null;

        min = null;
        max = null;
        origin = null;

        transform = null;

        flipX.clear();
        flipX = null;
        flipY.clear();
        flipY = null;

        for(int i = 0; i < materials.length; ++i) {
            materials[i].clear();
            materials[i] = null;
        }
        materials = null;

        for(int i = 0; i < meshes.length; ++i) {
            meshes[i].clear();
            meshes[i] = null;
        }
        meshes = null;
    }

    public Matrix4f getTransform() {
        return transform;
    }

    public float getDistance() {
        return distance;
    }

    public void rotate(float angle, Vector3f axis) {
        new Matrix4f().rotate(angle, axis).mul(transform, transform);
    }

    public void updateRotation(float dt) {
        if(enableRotation) {
            rotate(dt, axisOfRotation);
        }
    }

    public void renderUi() {
        nk_layout_row_dynamic(ctx, 120, 1);
        if (nk_group_begin_titled(ctx, "ModelSpin", "Model Spin", NK_WINDOW_BORDER | NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_TITLE)) {
            nk_layout_row_dynamic(ctx, 25, 2);
            if (nk_option_label(ctx, "Spin On", enableRotation)) {
                enableRotation = true;
            }
            if (nk_option_label(ctx, "Spin Off", !enableRotation)) {
                enableRotation = false;
            }
            nk_layout_row_dynamic(ctx, 20, 1);
            nk_text(ctx, "Rotation Of Axis:", NK_TEXT_LEFT);

            nk_layout_row_dynamic(ctx, 20, 3);
            if (nk_option_label(ctx, "X Axis", rotationAxis == 0)) {
                axisOfRotation = Constants.X_AXIS;
                rotationAxis = 0;
            }
            if (nk_option_label(ctx, "Y Axis", rotationAxis == 1)) {
                axisOfRotation = Constants.Y_AXIS;
                rotationAxis = 1;
            }
            if (nk_option_label(ctx, "Z Axis", rotationAxis == 2)) {
                axisOfRotation = Constants.Z_AXIS;
                rotationAxis = 2;
            }
            nk_group_end(ctx);
        }

        nk_layout_row_dynamic(ctx, 65, 1);
        if (nk_group_begin_titled(ctx, "Flip Texture Coordinates", "Flip Texture Coordinates", NK_WINDOW_BORDER | NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_TITLE)) {
            nk_layout_row_dynamic(ctx, 25, 2);
            flipX.renderUi();
            flipY.renderUi();
            nk_group_end(ctx);
        }

        if(modelSuccessfullyLoaded) {
            for (int i = 0; i < materials.length; ++i) {
                materials[i].renderUi();
            }
        }
    }
}
