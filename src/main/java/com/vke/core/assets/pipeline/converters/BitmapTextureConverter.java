package com.vke.core.assets.pipeline.converters;

import com.vke.api.assets.Protocols;
import com.vke.api.assets.anot.Converter;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.texture.*;
import com.vke.core.Context;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetConverter;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.image.Bitmap;
import com.vke.core.parsing.config.utils.EmptyConfigObject;
import com.vke.core.services2.Services;
import com.vke.utils.iter.helpers.Option;

@Converter
public class BitmapTextureConverter implements AssetConverter {
    @Override
    public String from() {
        return Protocols.BITMAP;
    }

    @Override
    public String to() {
        return Protocols.TEXTURE;
    }

    @Override
    public AssetData performConversion(Context context, StageElement input, ConfigArrayNode arguments) throws AssetException {
        Bitmap bitmap = input.getAssetData().getDataAs();

        ConfigNode meta = input.getMetaAttributes().getAssetConfig();

        if (meta == null) meta = new EmptyConfigObject();

        Format textureFormat = Format.valueOfOption(meta.getString("format")).unwrapOr(Format.RGBA8_SRGB);
        SampleCount textureSampleCount = SampleCount.valueOfOption(meta.getString("sample-count")).unwrapOr(SampleCount.X1);
        ImageTiling textureTiling = ImageTiling.valueOfOption(meta.getString("image-tiling")).unwrapOr(ImageTiling.OPTIMAL);
        boolean generateMips = meta.getBooleanOption("generate-mips").unwrapOrDefault();
        int mipLevels = meta.getIntOption("mip-levels").unwrapOrIdentity();

        ImageUsage usage = new ImageUsage();

        Option<String> textureUsageParts = meta.getStringOption("usage");
        if (textureUsageParts.isSome()) {
            String[] parts = textureUsageParts.unwrap().split(";");
            for (String part : parts) {
                usage.or(ImageUsage.Bits.valueOf(part));
            }
        } else {
            usage.or(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.TRANSFER_SRC_BIT, ImageUsage.Bits.TRANSFER_DST_BIT);
        }

        if (generateMips && mipLevels <= 0) throw new IllegalStateException("If a texture turns on generate-mips, mip-count must be greater than 0!");

        if (input.getAssetName().toString().equals("vke:textures/material/defaults/default_albedo.png")) {
            System.out.println();
        }

        Renderer renderer = context.service(Services.RENDERER);
        Texture.TextureDescriptorBuilder builder = Texture.TextureDesc.builder()
                .width(bitmap.width())
                .height(bitmap.height())
                .format(textureFormat)
                .sampleCount(textureSampleCount)
                .tiling(textureTiling)
                .generateMips(generateMips)
                .mipLevels(mipLevels)
                .usage(usage)
                .type(TextureType.TEX_2D);

        Texture texture = bitmap.createTexture2D(renderer, builder);
        return new AssetData(Protocols.TEXTURE, texture);
    }
}
