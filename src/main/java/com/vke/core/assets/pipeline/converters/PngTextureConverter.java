package com.vke.core.assets.pipeline.converters;

import com.vke.api.assets.Protocols;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.rendering.abstraction.RenderDevice;
import com.vke.api.rendering.abstraction.Renderer;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.enums.texture.Format;
import com.vke.api.rendering.abstraction.enums.texture.ImageUsage;
import com.vke.api.rendering.abstraction.enums.texture.TextureType;
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
        Texture.TextureDesc desc = Texture.TextureDesc.builder()
                .width(info.width)
                .height(info.height)
                .format(Format.RGBA8_SRGB)
                .usage(new ImageUsage(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.TRANSFER_SRC_BIT, ImageUsage.Bits.TRANSFER_DST_BIT))
                .type(TextureType.TEX_2D)
                .build();
        Texture texture = device.createTexture(png.getOutput(), desc);
        return new AssetData(Protocols.TEXTURE, texture);
    }
}
