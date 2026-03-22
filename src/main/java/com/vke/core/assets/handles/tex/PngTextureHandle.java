package com.vke.core.assets.handles.tex;

import com.vke.api.abstraction.RenderDevice;
import com.vke.api.abstraction.Renderer;
import com.vke.api.abstraction.data.Texture;
import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.Protocols;
import com.vke.core.Context;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.file.png.Pixels;
import com.vke.core.file.png.PngFile;
import com.vke.utils.io.Identifier;

import java.io.IOException;

public class PngTextureHandle implements AssetHandle<Texture> {

    private final Identifier id;

    private Texture texture;

    public PngTextureHandle(Identifier id) {
        this.id = id;
    }

    @Override
    public String getProtocol() {
        return Protocols.PNG;
    }

    @Override
    public Texture acquire(Context context) throws IOException {
        if (texture != null) return texture;

        EngineCreateInfo.RendererType rendererType = context.getEngine().rendererType();
        Renderer renderer = context.service(rendererType.serviceName);
        RenderDevice device = renderer.getDevice();
        PngFile pngFile = new PngFile(id.asInputStream());
        Pixels pixels = pngFile.getOutput();
        Texture.TextureDesc desc = getDescription(pngFile);

        this.texture = device.createTexture(pixels, desc);
        return this.texture;
    }

    private Texture.TextureDesc getDescription(PngFile pngFile) {
        // TODO: check xml file and stuff
        return Texture.TextureDesc.albedo2D(pngFile.getPngInfo().width, pngFile.getPngInfo().height);
    }

    @Override
    public Texture get() {
        return texture;
    }

    @Override
    public boolean isAvailable() {
        return texture != null;
    }

    @Override
    public void free() {
        if (texture != null) {
            texture.free();
        }
        texture = null;
    }
}
