package com.modelviewer.Renderer.Mesh;

import com.modelviewer.Renderer.Shader;
import com.modelviewer.Utils.Utils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL32.glDrawElementsBaseVertex;

public class Model {
    private MeshInfo[] meshes;
    private Material[] materials;

    private float[] positions;
    private float[] normals;
    private float[] texCords;
    private float[] colors;
    private int[] indices;

    private static final int INDEX_BUFFER = 0;
    private static final int POSITION_BUFFER = 1;
    private static final int TEX_COORD_BUFFER = 2;
    private static final int NORMAL_BUFFER = 3;
    private static final int COLOR_BUFFER = 4;
    private static final int NUMBER_OF_BUFFER = 5;

    private int vaoID;
    private int[] bufferIDs = new int[NUMBER_OF_BUFFER];

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
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        for(int i = 0; i < bufferIDs.length; ++i) {
            bufferIDs[i] = glGenBuffers();
        }

        boolean ret = true;

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
            ret = initFromScene(pScene, filePath);
            processMeshTransform(pScene.mRootNode(), new Matrix4f());
            adjustTransform();
        } else {
            ret = false;
            System.out.println("Error parsing: " + filePath + " " + Assimp.aiGetErrorString().toString());
            System.exit(-1);
        }

        glBindVertexArray(0);

        modelSuccessfullyLoaded = ret;

        return ret;
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

    // private void clear() {}

    public void render(Shader shader) {
        shader.activate();
        glBindVertexArray(vaoID);

        if(modelSuccessfullyLoaded) {
            for (int i = 0; i < meshes.length; ++i) {
                shader.uploadN("modelTransform", meshes[i].modelTransform);
                glDrawElementsBaseVertex(GL_TRIANGLES, meshes[i].numberOfIndices, GL_UNSIGNED_INT, meshes[i].baseIndex * 4, meshes[i].baseVertex);
            }
        }

        glBindVertexArray(0);
        shader.deactivate();
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
        positions = new float[numberOfVertices * 3];
        colors = new float[numberOfVertices * 4];
        normals = new float[numberOfVertices * 3];
        texCords = new float[numberOfVertices * 2];
        indices = new int[numberOfIndices * 3];
    }

    private void initAllMeshes(AIScene pScene) {
        PointerBuffer buffer = pScene.mMeshes();
        int v = 0, t = 0, n = 0, ii = 0, c = 0;

        for (int j = 0 ; j < meshes.length ; j++) {
            AIMesh mesh = AIMesh.create(buffer.get(j));

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
        }
    }

    // private boolean initMaterials(AIScene pScene, String filePath) {}

    private void loadBuffers() {
        glBindVertexArray(vaoID);

        glBindBuffer(GL_ARRAY_BUFFER, bufferIDs[POSITION_BUFFER]);
        glBufferData(GL_ARRAY_BUFFER, Utils.convertFloatArrayToFloatBuffer(positions), GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, bufferIDs[TEX_COORD_BUFFER]);
        glBufferData(GL_ARRAY_BUFFER, Utils.convertFloatArrayToFloatBuffer(texCords), GL_STATIC_DRAW);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1,  2, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, bufferIDs[NORMAL_BUFFER]);
        glBufferData(GL_ARRAY_BUFFER, Utils.convertFloatArrayToFloatBuffer(normals), GL_STATIC_DRAW);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, bufferIDs[COLOR_BUFFER]);
        glBufferData(GL_ARRAY_BUFFER, Utils.convertFloatArrayToFloatBuffer(colors), GL_STATIC_DRAW);
        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 4, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferIDs[INDEX_BUFFER]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, Utils.convertIntArrayToIntBuffer(indices), GL_STATIC_DRAW);

        glBindVertexArray(0);
    }

    public Matrix4f getTransform() {
        return transform;
    }

    public float getDistance() {
        return distance;
    }
}
