package com.vke.core.assets.pipeline.converters;

import com.vke.api.assets.Protocols;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.rendering.abstraction.RenderDevice;
import com.vke.api.rendering.abstraction.Renderer;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.core.Context;
import com.vke.core.EngineCreateInfo;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetConverter;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.file.png.PngFile;
import com.vke.core.file.png.PngInfo;

public class PngTextureConverter implements AssetConverter {
    @Override
    public String from() {
        return Protocols.PNG;
    }

    @Override
    public String to() {
        return Protocols.TEXTURE;
    }

    @Override
    public AssetData performConversion(Context context, StageElement input, ConfigArrayNode arguments) throws AssetException {
        PngFile png = input.getAssetData().getDataAs();
        PngInfo info = png.getPngInfo();
        EngineCreateInfo.RendererType rt = context.getEngine().rendererType();
        Renderer renderer = context.service(rt.serviceName);
        RenderDevice device = renderer.getDevice();
        Texture texture = device.createTexture(png.getOutput(), Texture.TextureDesc.albedo2D(info.width, info.height));
        return new AssetData(Protocols.TEXTURE, texture);
    }
}
