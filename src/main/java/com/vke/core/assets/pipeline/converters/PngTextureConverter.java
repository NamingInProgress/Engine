package com.vke.core.assets.pipeline.converters;

import com.vke.api.assets.Protocols;
import com.vke.api.assets.anot.Converter;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.rendering.abstraction.renderer.RenderDevice;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.texture.Format;
import com.vke.api.rendering.abstraction.renderer.enums.texture.ImageUsage;
import com.vke.api.rendering.abstraction.renderer.enums.texture.TextureType;
import com.vke.core.Context;
import com.vke.core.EngineCreateInfo;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetConverter;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.file.png.PngFile;
import com.vke.core.file.png.PngInfo;
import com.vke.core.services2.Services;

@Converter
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
        Renderer renderer = context.service(Services.RENDERER);
        RenderDevice device = renderer.getDevice();
        Texture.TextureDesc desc = Texture.TextureDesc.builder()
                .width(info.width)
                .height(info.height)
                .format(Format.RGBA8)
                .usage(new ImageUsage(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.TRANSFER_SRC_BIT, ImageUsage.Bits.TRANSFER_DST_BIT))
                .type(TextureType.TEX_2D)
                .build();
        Texture texture = device.createTexture(desc);
        texture.upload(png.getOutput());
        return new AssetData(Protocols.TEXTURE, texture);
    }
}
