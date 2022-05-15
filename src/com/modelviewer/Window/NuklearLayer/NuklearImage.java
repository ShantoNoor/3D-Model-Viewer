package com.modelviewer.Window.NuklearLayer;

import com.modelviewer.Renderer.Shader.ShaderProgram;
import com.modelviewer.Renderer.Texture;
import com.modelviewer.Utils.Constants;
import com.modelviewer.Utils.Utils;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkImage;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog;

public class NuklearImage {
    private NkContext ctx;
    private NkImage image;
    private Texture texture;
    private NuklearCheckbox use;
    private String name, shaderSamplerName;
    private boolean textureLoadedSuccessfully, asRed;
    private int textureSlot;

    public NuklearImage(NkContext ctx, String name, Boolean asRed, String shaderSamplerName, int textureSlot) {
        this.ctx = ctx;
        this.name = name;
        this.asRed = asRed;
        this.shaderSamplerName = shaderSamplerName;
        this.textureSlot = textureSlot;

        use = new NuklearCheckbox(ctx, "Use");
        use.set(0);
        image = NkImage.create();
        image.handle(it -> it.id(Constants.nullTexture.getId()));
    }

    public void renderUi() {
        nk_layout_row_dynamic(ctx, 285, 1);
        if (nk_group_begin_titled(ctx, name+Float.toString(Utils.getTime()), name, NK_WINDOW_BORDER | NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_TITLE)) {
            nk_layout_row_begin(ctx, NK_DYNAMIC, nk_window_get_width(ctx) - 90, 2);
            nk_layout_row_push(ctx, 0.8f);
            if(nk_button_image(ctx, image)) {
                loadTexture();
            }
            nk_layout_row_push(ctx, 0.2f);

            if(textureLoadedSuccessfully) {
                use.renderUi();
            }

            nk_layout_row_dynamic(ctx, 25, 2);
            if (nk_button_label(ctx, "Select")) {
                loadTexture();
            }
            if (nk_button_label(ctx, "Clear")) {
                tempClear();
            }
            nk_group_end(ctx);
        }
    }

    private void loadTexture() {
        String filePath = tinyfd_openFileDialog(name, "", null, "", false);

        if(filePath == null) return;

        if(texture != null) {
            tempClear();
        }

        texture = new Texture();

        if(texture.init(filePath, asRed)) {
            image.handle(it -> it.id(texture.getId()));
            textureLoadedSuccessfully = true;
            use.set(1);
        }
    }

    private void tempClear() {
        if(!textureLoadedSuccessfully) return;
        texture.clear();
        texture = null;
        image.handle(it -> it.id(Constants.nullTexture.getId()));
        textureLoadedSuccessfully = false;
        use.set(0);
    }

    public void clear() {
        ctx = null;
        image.clear();
        image = null;
        if(texture != null) texture.clear();
        texture = null;
        use.clear();
        use = null;
        name = null;
        shaderSamplerName = null;
    }

    public void upload(ShaderProgram shaderProgram) {
        if(textureLoadedSuccessfully) {
            texture.bind(shaderProgram, shaderSamplerName + ".sampler", textureSlot);
        }
        shaderProgram.upload(shaderSamplerName + ".useSampler", use.get());
    }

    public void safeUpload(ShaderProgram shaderProgram) {
        if(textureLoadedSuccessfully) {
            texture.safeBind(shaderProgram, shaderSamplerName + ".sampler", textureSlot);
        }
        shaderProgram.safeUpload(shaderSamplerName + ".useSampler", use.get());
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addMaterialName(String materialName) {
        this.name += " (" + materialName + ")";
    }
}
