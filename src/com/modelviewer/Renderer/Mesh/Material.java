package com.modelviewer.Renderer.Mesh;

import com.modelviewer.Renderer.Shader.ShaderProgram;
import com.modelviewer.Window.NuklearLayer.NuklearColor;
import com.modelviewer.Window.NuklearLayer.NuklearFloat;
import com.modelviewer.Window.NuklearLayer.NuklearImage;
import org.joml.Vector4f;
import org.lwjgl.nuklear.NkContext;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.nuklear.Nuklear.nk_group_end;

public class Material {
    private NkContext ctx;

    public String materialName;

    public NuklearColor ambientColor;
    public NuklearColor diffuseColor;
    public NuklearColor specularColor;
    public NuklearColor emissiveColor;
    public NuklearFloat roughnessFactor;
    public NuklearFloat metallicFactor;
    public NuklearFloat shininess;
    public NuklearFloat emissiveIntensity;
    public NuklearImage baseColorMap;
    public NuklearImage normalMap;
    public NuklearImage aoMap;
    public NuklearImage metalnessMap;
    public NuklearImage roughnessMap;

    public Material(NkContext ctx) {
        this.ctx = ctx;

        materialName = "Material";

        ambientColor = new NuklearColor(ctx, "Ambient Color", new Vector4f(0.1f, 0.1f, 0.1f, 1.0f), "ambientColor");
        diffuseColor = new NuklearColor(ctx, "Diffuse Color", new Vector4f(0.3f, 0.3f, 0.3f, 1.0f), "diffuseColor");
        specularColor = new NuklearColor(ctx, "Specular Color", new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), "specularColor");
        emissiveColor  = new NuklearColor(ctx, "Emissive Color", new Vector4f(0.0f, 0.0f, 0.0f, 1.0f), "emissiveColor");

        roughnessFactor  = new NuklearFloat(ctx, "Roughness Factor", 0.10f, 0.0f, 1.0f, 0.005f, 0.001f, "roughnessFactor");
        metallicFactor  = new NuklearFloat(ctx, "Metallic Factor", 1.0f, 0.0f, 1.0f, 0.005f, 0.001f, "metallicFactor");
        shininess  = new NuklearFloat(ctx, "Shininess", 4.0f, 0.0f, 1000.0f, 0.05f, 0.01f, "shininess");
        emissiveIntensity  = new NuklearFloat(ctx, "Emissive Intensity", 1.0f, 0.0f, 1.0f, 0.005f, 0.001f, "emissiveIntensity");

        baseColorMap = new NuklearImage(ctx, "Albedo Map", false, "baseColorMap", 0);
        normalMap = new NuklearImage(ctx, "Normal Map", false, "normalMap", 1);
        aoMap = new NuklearImage(ctx, "Ambient Occlusion Map", true, "aoMap", 2);
        roughnessMap = new NuklearImage(ctx, "Roughness Map", true, "roughnessMap", 3);
        metalnessMap = new NuklearImage(ctx, "Metallic Map", true, "metallicMap", 4);
    }

    public void setNames(int i) {
        materialName += " " + Integer.toString(i+1);
        ambientColor.addMaterialName(materialName);
        diffuseColor.addMaterialName(materialName);
        specularColor.addMaterialName(materialName);
        emissiveColor.addMaterialName(materialName);
        roughnessFactor.addMaterialName(materialName);
        metallicFactor.addMaterialName(materialName);
        shininess.addMaterialName(materialName);
        emissiveIntensity.addMaterialName(materialName);
        baseColorMap.addMaterialName(materialName);
        normalMap.addMaterialName(materialName);
        aoMap.addMaterialName(materialName);
        metalnessMap.addMaterialName(materialName);
        roughnessMap.addMaterialName(materialName);
    }

    public void renderUi() {
        nk_layout_row_dynamic(ctx, 2000, 1);
        if(nk_group_begin_titled(ctx, materialName, materialName, NK_WINDOW_BORDER | NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_TITLE)) {
            ambientColor.renderUi();
            diffuseColor.renderUi();
            specularColor.renderUi();
            emissiveColor.renderUi();
            roughnessFactor.renderUi();
            metallicFactor.renderUi();
            shininess.renderUi();
            emissiveIntensity.renderUi();
            baseColorMap.renderUi();
            normalMap.renderUi();
            aoMap.renderUi();
            metalnessMap.renderUi();
            roughnessMap.renderUi();
            nk_group_end(ctx);
        }
    }

    public void upload(ShaderProgram shaderProgram) {
        ambientColor.upload(shaderProgram);
        diffuseColor.upload(shaderProgram);
        specularColor.upload(shaderProgram);
        emissiveColor.upload(shaderProgram);
        roughnessFactor.upload(shaderProgram);
        metallicFactor.upload(shaderProgram);
        shininess.upload(shaderProgram);
        emissiveIntensity.upload(shaderProgram);
        baseColorMap.upload(shaderProgram);
        normalMap.upload(shaderProgram);
        aoMap.upload(shaderProgram);
        metalnessMap.upload(shaderProgram);
        roughnessMap.upload(shaderProgram);
    }

    public void safeUpload(ShaderProgram shaderProgram) {
        ambientColor.safeUpload(shaderProgram);
        diffuseColor.safeUpload(shaderProgram);
        specularColor.safeUpload(shaderProgram);
        emissiveColor.safeUpload(shaderProgram);
        roughnessFactor.safeUpload(shaderProgram);
        metallicFactor.safeUpload(shaderProgram);
        shininess.safeUpload(shaderProgram);
        emissiveIntensity.safeUpload(shaderProgram);
        baseColorMap.safeUpload(shaderProgram);
        normalMap.safeUpload(shaderProgram);
        aoMap.safeUpload(shaderProgram);
        metalnessMap.safeUpload(shaderProgram);
        roughnessMap.safeUpload(shaderProgram);
    }

    public void clear() {
        materialName = null;
        ctx = null;

        ambientColor.clear();
        diffuseColor.clear();
        specularColor.clear();
        emissiveColor.clear();
        roughnessFactor.clear();
        metallicFactor.clear();
        shininess.clear();
        emissiveIntensity.clear();
        baseColorMap.clear();
        normalMap.clear();
        aoMap.clear();
        metalnessMap.clear();
        roughnessMap.clear();

        ambientColor = null;
        diffuseColor = null;
        specularColor = null;
        emissiveColor = null;
        roughnessFactor = null;
        metallicFactor = null;
        shininess = null;
        emissiveIntensity = null;
        baseColorMap = null;
        normalMap = null;
        aoMap = null;
        metalnessMap = null;
        roughnessMap = null;
    }
}
