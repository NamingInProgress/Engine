package com.vke.core.assets.pipeline.protocols;

import com.vke.api.assets.Protocols;
import com.vke.api.rendering.abstraction.RenderDevice;
import com.vke.api.rendering.abstraction.Renderer;
import com.vke.api.rendering.abstraction.data.Texture;
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
                Texture.TextureDesc desc = Texture.TextureDesc.albedo2D(pngFile.getPngInfo().width, pngFile.getPngInfo().height);

                EngineCreateInfo.RendererType rendererType = context.getEngine().rendererType();
                Renderer renderer = context.service(rendererType.serviceName);
                RenderDevice device = renderer.getDevice();

                return new AssetData(Protocols.PNG, device.createTexture(pixels, desc));
            });
        }
    }

}
