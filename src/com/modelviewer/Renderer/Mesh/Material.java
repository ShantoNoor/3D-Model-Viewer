package com.modelviewer.Renderer.Mesh;

import com.modelviewer.Renderer.Texture;
import org.joml.Vector3f;

public class Material {
    Vector3f AmbientColor = new Vector3f(0.0f, 0.0f, 0.0f);
    Vector3f DiffuseColor = new Vector3f(0.0f, 0.0f, 0.0f);
    Vector3f SpecularColor = new Vector3f(0.0f, 0.0f, 0.0f);

    // TODO: need to deallocate these
    Texture pDiffuse = null; // base color of the material
    Texture pSpecularExponent = null;
}
