package com.vke.core.assets.pipeline.protocols.texture;

import com.vke.api.assets.Protocols;
import com.vke.api.assets.anot.Protocol;
import com.vke.api.rendering.abstraction.renderer.RenderDevice;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.texture.Format;
import com.vke.api.rendering.abstraction.renderer.enums.texture.ImageUsage;
import com.vke.api.rendering.abstraction.renderer.enums.texture.TextureType;
import com.vke.core.Context;
import com.vke.core.EngineCreateInfo;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.Op;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.apis.AssetUri;
import com.vke.core.assets.pipeline.stages.PipelineStage;
import com.vke.core.file.png.Pixels;
import com.vke.core.file.png.PngFile;
import com.vke.utils.Utils;
import com.vke.utils.io.Identifier;

@Protocol
public class PngProtocol implements AssetProtocol<Texture> {
    @Override
    public String getProtocolName() {
        return Protocols.PNG;
    }

    @Override
    public AssetData getField(AssetData data, AssetUri uri) throws AssetException {
        throw AssetException.unknownSelector(getProtocolName(), uri.getSelector());
    }

    @Override
    public Loader getLoader() {
        return new PngTextureLoader();
    }

    @Override
    public boolean applies(AssetData a, AssetData b, Op op) {
        return false;
    }

    public static class PngTextureLoader implements Loader {

        @Override
        public AssetData load(Context context, Identifier identifier, PipelineStage.ExecutionTarget executionTarget) throws AssetException {
            return Utils.chainExceptions(() -> {
                PngFile pngFile = new PngFile(identifier.asInputStream());
                Pixels pixels = pngFile.getOutput();
                Texture.TextureDesc desc = Texture.TextureDesc.builder()
                        .width(pngFile.getPngInfo().width)
                        .height(pngFile.getPngInfo().height)
                        .format(Format.RGBA8_SRGB)
                        .usage(new ImageUsage(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.TRANSFER_SRC_BIT, ImageUsage.Bits.TRANSFER_DST_BIT))
                        .type(TextureType.TEX_2D)
                        .build();

                EngineCreateInfo.RendererType rendererType = context.getEngine().rendererType();
                Renderer renderer = context.service(rendererType.serviceName);
                RenderDevice device = renderer.getDevice();

                return new AssetData(Protocols.PNG, device.createTexture(desc).upload(pixels));
            });
        }
    }

}
