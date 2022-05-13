package com.modelviewer.Renderer.Mesh;

import com.modelviewer.Renderer.*;
import com.modelviewer.Renderer.Shader.ShaderProgram;
import com.modelviewer.Utils.Utils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL32.glDrawElementsBaseVertex;

public class Model {
    private MeshInfo[] meshes;
    private Texture[] textures;

    private float[] positions;
    private float[] normals;
    private float[] texCords;
    private float[] colors;
    private int[] indices;

    private FloatBuffer tangents, bitangents;

    private static final int POSITION_BUFFER = 0;
    private static final int TEX_COORD_BUFFER = 1;
    private static final int NORMAL_BUFFER = 2;
    private static final int COLOR_BUFFER = 3;
    private static final int TANGENT_BUFFER = 4;
    private static final int BITANGENT_BUFFER = 5;
    private static final int NUMBER_OF_BUFFER = 6;

    private int haveTangents;

    private VAO vao = new VAO();
    private VBO[] vbos = new VBO[NUMBER_OF_BUFFER];
    private IBO ibo = new IBO();

    private int numberOfVertices = 0, numberOfIndices = 0;

    private boolean modelSuccessfullyLoaded = false;

    private Vector3f min = new Vector3f(Float.MAX_VALUE);
    private Vector3f max = new Vector3f(Float.MIN_VALUE);
    private Vector3f origin = new Vector3f(0.0f);
    private float distance;
    private float minDistance = 15.0f;
    private float maxDistance = 30.0f;

    private Matrix4f transform = new Matrix4f();

    public Model() { }

    public boolean loadMesh(String filePath) {
        if(modelSuccessfullyLoaded) return !modelSuccessfullyLoaded;

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
            processMeshTransform(pScene.mRootNode(), new Matrix4f());
            adjustTransform();
        } else {
            modelSuccessfullyLoaded = false;
            System.out.println("Error parsing: " + filePath + " " + Assimp.aiGetErrorString().toString());
            System.exit(-1);
        }

         printAllMaterials(pScene);

        return modelSuccessfullyLoaded;
    }

    private void printAllMaterials(AIScene scene) {
        PointerBuffer materials = scene.mMaterials();

        for (int i = 0; i < scene.mNumMaterials(); ++i) {
            AIMaterial material = AIMaterial.create(materials.get(i));

            PointerBuffer materialProperties = material.mProperties();
            for (int j = 0; j < material.mNumProperties(); ++j) {
                AIMaterialProperty property = AIMaterialProperty.create(materialProperties.get(j));
                String key = property.mKey().dataString();

                System.out.println(key);

                if(key.equals(Assimp.AI_MATKEY_NAME)) {
                    System.out.println(Utils.convertByteBufferToString(property.mData()));
                }

                if(key.equals(Assimp._AI_MATKEY_TEXTURE_BASE)) {
                    System.out.println(Assimp.TextureTypeToString(property.mSemantic()) + ": " + Utils.convertByteBufferToString(property.mData()));
                }

//                if(key.equals(Assimp.ai_AI_MATKEY_GLTF_MAPPINGFILTER_MAG_BASE)) {
//                    System.out.println(Utils.convertByteBufferToString(property.mData()));
//                }

//                System.out.println("Material: (" + i + ", " + j + "): ");
                if(key.equals(Assimp.AI_MATKEY_GLOSSINESS_FACTOR)) {
                    System.out.println("GLOSSINESS_FACTOR: " + property.mData().getFloat());
                } else if(key.equals(Assimp.AI_MATKEY_OPACITY)) {
                    System.out.println("OPACITY: " + property.mData().getFloat());
                } else if(key.equals(Assimp.AI_MATKEY_REFLECTIVITY)) {
                    System.out.println("REFLECTIVITY: " + property.mData().getFloat());
                } else if(key.equals(Assimp.AI_MATKEY_CLEARCOAT_FACTOR)) {
                    System.out.println("CLEARCOAT_FACTOR: " + property.mData().getFloat());
                } else if(key.equals(Assimp.AI_MATKEY_METALLIC_FACTOR)) {
                    System.out.println("METALLIC_FACTOR: " + property.mData().getFloat());
                } else if(key.equals(Assimp.AI_MATKEY_EMISSIVE_INTENSITY)) {
                    System.out.println("EMISSIVE_INTENSITY: " + property.mData().getFloat());
                } else if(key.equals(Assimp.AI_MATKEY_ROUGHNESS_FACTOR)) {
                    System.out.println("ROUGHNESS_FACTOR: " + property.mData().getFloat());
                } else if(key.equals(Assimp.AI_MATKEY_SHININESS)) {
                    System.out.println("SHININESS: " + property.mData().getFloat());
                } else if(key.equals(Assimp.AI_MATKEY_USE_EMISSIVE_MAP)) {
                    System.out.println("EMISSIVE_MAP: ");
//                    System.out.println("EMISSIVE_MAP: " + property.mData().getChar());
                } else if(key.equals(Assimp.AI_MATKEY_USE_COLOR_MAP)) {
                    System.out.println("COLOR_MAP: ");
//                    System.out.println("COLOR_MAP: " + property.mData().getChar());
                } else if(key.equals(Assimp.AI_MATKEY_USE_ROUGHNESS_MAP)) {
                    System.out.println("ROUGHNESS_MAP: ");
//                    System.out.println("ROUGHNESS_MAP: " + property.mData().getChar());
                } else if(key.equals(Assimp.AI_MATKEY_USE_AO_MAP)) {
                    System.out.println("AO_MAP: ");
//                    System.out.println("AO_MAP: " + property.mData().getChar());
                } else if(key.equals(Assimp.AI_MATKEY_USE_METALLIC_MAP)) {
                    System.out.println("METALLIC_MAP: ");
//                    System.out.println("METALLIC_MAP: " + property.mData().getChar());
                }
//                System.out.println();

            }

//            AIString path;
//            for(int j = 1; j < 22; ++j) {
//                for (int k = 0; k < 10; ++k) {
//                    path = AIString.create();
//                    Assimp.aiGetMaterialTexture(material, j, k, path, (IntBuffer) null, (IntBuffer) null, null, null, null, null);
//                    if(path.dataString() != "") System.out.println(Assimp.TextureTypeToString(j) + "( material: "+ i + ", index: " + k + " ): " + path.dataString());
//                }
//            }
        }

//        PointerBuffer textures = scene.mTextures();
//        for(int i = 0; i < scene.mNumTextures(); ++i) {
//            AITexture texture = AITexture.create(textures.get(i));
//            System.out.println(texture.mFilename().dataString());
//            System.out.println(texture.mWidth() + " " + texture.mHeight());
//        }
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

        if(modelSuccessfullyLoaded) {
            for (int i = 0; i < meshes.length; ++i) {
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
        textures = new Texture[pScene.mNumMaterials()];

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
        positions = new float[numberOfVertices * 3];
        colors = new float[numberOfVertices * 4];
        normals = new float[numberOfVertices * 3];
        texCords = new float[numberOfVertices * 2];
        indices = new int[numberOfIndices * 3];

        tangents = BufferUtils.createFloatBuffer(numberOfVertices * 3);
        bitangents = BufferUtils.createFloatBuffer(numberOfVertices * 3);
    }

    private void initAllMeshes(AIScene pScene) {
        PointerBuffer buffer = pScene.mMeshes();
        int v = 0, t = 0, n = 0, ii = 0, c = 0;

        for (int j = 0 ; j < meshes.length ; j++) {
            AIMesh mesh = AIMesh.create(buffer.get(j));

            System.out.println("Mesh " + j + ": " + mesh.mName().dataString() + ": material index: " + mesh.mMaterialIndex());

            AIVector3D.Buffer vertexPositions = mesh.mVertices();
            for(int i = 0; i < vertexPositions.limit(); ++i) {
                AIVector3D vertexPosition = vertexPositions.get(i);

                positions[v++] = vertexPosition.x();
                positions[v++] = vertexPosition.y();
                positions[v++] = vertexPosition.z();
            }

            AIVector3D.Buffer vertexTexCoords = mesh.mTextureCoords(0);
            if(vertexTexCoords != null)
                for(int i = 0; i < vertexTexCoords.limit(); ++i) {
                    AIVector3D vertexTexCoord = vertexTexCoords.get(i);

                    texCords[t++] = vertexTexCoord.x();
                    texCords[t++] = vertexTexCoord.y();
                }

            AIVector3D.Buffer vertexNormals = mesh.mNormals();
            if(vertexNormals != null)
                for(int i = 0; i < vertexNormals.limit(); i++) {
                    AIVector3D vertexNormal = vertexNormals.get(i);

                    normals[n++] = vertexNormal.x();
                    normals[n++] = vertexNormal.y();
                    normals[n++] = vertexNormal.z();
                }

            AIColor4D.Buffer vertexColours = mesh.mColors(0);
            if(vertexColours != null)
                for(int i = 0; i < vertexColours.limit(); i++) {
                    AIColor4D vertexColor = vertexColours.get(i);

                    colors[c++] = vertexColor.r();
                    colors[c++] = vertexColor.g();
                    colors[c++] = vertexColor.b();
                    colors[c++] = vertexColor.a();
                }

            for(int i = 0; i < mesh.mNumFaces(); i++) {
                AIFace face = mesh.mFaces().get(i);

                indices[ii++] = face.mIndices().get(0);
                indices[ii++] = face.mIndices().get(1);
                indices[ii++] = face.mIndices().get(2);
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

    // private boolean initMaterials(AIScene pScene, String filePath) {}

    private void loadBuffers() {
        vao.bind();
        for(int i = 0; i < vbos.length; ++i) {
            vbos[i] = new VBO();
        }

        vbos[POSITION_BUFFER].uploadVertexAttributeData(vao, positions, POSITION_BUFFER, 3, BufferDataType.STATIC);
        vbos[TEX_COORD_BUFFER].uploadVertexAttributeData(vao, texCords, TEX_COORD_BUFFER, 2, BufferDataType.STATIC);
        vbos[NORMAL_BUFFER].uploadVertexAttributeData(vao, normals, NORMAL_BUFFER, 3, BufferDataType.STATIC);
        vbos[COLOR_BUFFER].uploadVertexAttributeData(vao, colors, COLOR_BUFFER, 4, BufferDataType.STATIC);

        if(haveTangents > 0) {
            vbos[TANGENT_BUFFER].uploadVertexAttributeData(vao, tangents.flip(), TANGENT_BUFFER, 3, BufferDataType.STATIC);
            vbos[BITANGENT_BUFFER].uploadVertexAttributeData(vao, bitangents.flip(), BITANGENT_BUFFER, 3, BufferDataType.STATIC);
        }

        ibo.uploadIndicesData(vao, indices, BufferDataType.STATIC);
        vao.unbind();
    }

    public void clear() {
        vao.clear();
        for(int i = 0; i < vbos.length; ++i) {
            vbos[i].clear();
        }
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
}
