package com.modelviewer.Renderer.Mesh;

import com.modelviewer.Window.NuklearLayer.NuklearColor;
import com.modelviewer.Window.NuklearLayer.NuklearFloat;
import com.modelviewer.Window.NuklearLayer.NuklearImage;
import org.joml.Vector4f;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.system.MemoryStack;

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
    public NuklearFloat reflectivity;
    public NuklearFloat shininessIntensity;
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

        roughnessFactor  = new NuklearFloat(ctx, "Roughness Factor", 0.0f, 0.0f, 1.0f, 0.005f, 0.001f, "roughnessFactor");
        metallicFactor  = new NuklearFloat(ctx, "Metallic Factor", 1.0f, 0.0f, 1.0f, 0.005f, 0.001f, "metallicFactor");
        shininess  = new NuklearFloat(ctx, "Shininess", 4.0f, 1.0f, 100.0f, 0.05f, 0.01f, "shininess");
        reflectivity  = new NuklearFloat(ctx, "Reflectivity", 0.0f, 1.0f, 100.0f, 0.05f, 0.01f, "reflectivity");
        shininessIntensity  = new NuklearFloat(ctx, "Shininess Intensity", 1.0f, 0.0f, 1.0f, 0.005f, 0.001f, "shininessIntensity");
        emissiveIntensity  = new NuklearFloat(ctx, "Mmissive Intensity", 1.0f, 0.0f, 1.0f, 0.005f, 0.001f, "emissiveIntensity");

        baseColorMap = new NuklearImage(ctx, "Albedo Map", false, "baseColor", 0);
        normalMap = new NuklearImage(ctx, "Normal Map", false, "normalMap", 1);
        aoMap = new NuklearImage(ctx, "Ambient Occlusion Map", true, "aoMap", 2);
        roughnessMap = new NuklearImage(ctx, "Roughness Map", true, "roughnessMap", 3);
        metalnessMap = new NuklearImage(ctx, "Metallic Map", true, "metallicMap", 4);
    }

    public void updateAndRenderUi(MemoryStack stack) {
        nk_layout_row_dynamic(ctx, 1838, 1);
        if(nk_group_begin_titled(ctx, materialName, materialName, NK_WINDOW_BORDER | NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_TITLE)) {
            ambientColor.updateAndRenderUi(stack);
            diffuseColor.updateAndRenderUi(stack);
            specularColor.updateAndRenderUi(stack);
            emissiveColor.updateAndRenderUi(stack);
            roughnessFactor.updateAndRenderUi();
            metallicFactor.updateAndRenderUi();
            shininess.updateAndRenderUi();
            reflectivity.updateAndRenderUi();
            shininessIntensity.updateAndRenderUi();
            emissiveIntensity.updateAndRenderUi();
            baseColorMap.updateAndRenderUi();
            normalMap.updateAndRenderUi();
            metalnessMap.updateAndRenderUi();
            roughnessMap.updateAndRenderUi();
            nk_group_end(ctx);
        }
    }
}
