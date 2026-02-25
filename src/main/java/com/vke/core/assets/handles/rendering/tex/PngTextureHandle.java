package com.vke.core.assets.handles.rendering.tex;

import com.vke.api.abstraction.RenderDevice;
import com.vke.api.abstraction.Renderer;
import com.vke.api.abstraction.data.Texture;
import com.vke.api.abstraction.shader.Shader;
import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.AssetUnavailableException;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.assets.handles.rendering.RenderingAssetHandle;
import com.vke.core.file.png.Pixels;
import com.vke.core.file.png.PngFile;
import com.vke.utils.Identifier;

import java.io.IOException;

public class PngTextureHandle extends RenderingAssetHandle<Texture> {
    private final Identifier id;

    public PngTextureHandle(Identifier id) {
        this.id = id;
    }

    @Override
    public Type getType() {
        return Type.Texture;
    }

    @Override
    protected Texture acquire(VKEngine engine, RenderDevice device) throws IOException {
        PngFile pngFile = new PngFile(id.asInputStream());
        Pixels pixels = pngFile.getOutput();
        Texture.TextureDesc desc = getDescription(pngFile);

        return device.createTexture(pixels, desc);
    }

    private Texture.TextureDesc getDescription(PngFile pngFile) {
        // TODO: check xml file and stuff
        return Texture.TextureDesc.albedo2D(pngFile.getPngInfo().width, pngFile.getPngInfo().height);
    }

    @Override
    public void free() {
        if (isAvailable()) {
            get().free();
        }
        setCache(null);
    }
}
