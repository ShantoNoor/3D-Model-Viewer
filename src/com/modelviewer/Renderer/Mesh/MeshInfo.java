package com.modelviewer.Renderer.Mesh;

import com.modelviewer.Utils.Constants;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class MeshInfo {
    public int numberOfVertices;
    public int numberOfIndices;
    public int baseVertex;
    public int baseIndex;
    public int materialIndex;
    public Matrix4f modelTransform;
    public Vector3f min, max;

    public MeshInfo() {
        this.numberOfVertices = 0;
        this.numberOfIndices = 0;
        this.baseVertex = 0;
        this.baseIndex = 0;
        this.materialIndex = Constants.INVALID_MATERIAL;
        this.modelTransform = new Matrix4f();
        this.min = new Vector3f();
        this.max = new Vector3f();
    }
}
