package com.vke.core.assets.pipeline.protocols.pipeline;

import com.vke.api.assets.Protocols;
import com.vke.api.assets.anot.Protocol;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.rendering.vulkan.pipeline.RenderPipelineData;
import com.vke.api.rendering.abstraction.renderer.RenderDevice;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.rendering.abstraction.renderer.pipeline.RenderPipeline;
import com.vke.core.Context;
import com.vke.core.EngineCreateInfo;
import com.vke.core.FileIdentifier;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.Op;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.apis.AssetUri;
import com.vke.core.assets.pipeline.stages.PipelineStage;
import com.vke.utils.Utils;

import java.io.IOException;

@Protocol
public class RenderPipelineProtocol implements AssetProtocol<RenderPipeline> {
    @Override
    public String getProtocolName() {
        return Protocols.RENDER_PIPELINE;
    }

    @Override
    public AssetData getField(AssetData data, AssetUri uri) throws AssetException {
        throw AssetException.unknownSelector(getProtocolName(), uri.getSelector());
    }

    @Override
    public Loader getLoader() {
        return new RenderPipelineLoader();
    }

    @Override
    public boolean applies(AssetData a, AssetData b, Op op) {
        return false;
    }

    public static AssetData fromConfig(Context context, ConfigDocument configDocument) throws AssetException {
        return Utils.chainExceptions(() -> {
            RenderPipelineData data = RenderPipelineData.fromConfig(configDocument);

            EngineCreateInfo.RendererType rendererType = context.getEngine().rendererType();
            Renderer renderer = context.service(rendererType.serviceName);
            RenderDevice device = renderer.getDevice();

            return new AssetData(Protocols.RENDER_PIPELINE, device.createRenderPipeline(data));
        });
    }

    public static class RenderPipelineLoader implements Loader {

        @Override
        public AssetData load(Context context, FileIdentifier identifier, PipelineStage.ExecutionTarget executionTarget) throws AssetException {
            if (!executionTarget.isUsable(PipelineStage.ExecutionTarget.Main)) return null;
            try {
                return fromConfig(context, ConfigDocument.parseIdentifier(identifier));
            } catch (IOException e) {
                throw new AssetException(e);
            }
        }
    }
}
