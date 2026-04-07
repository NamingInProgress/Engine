package com.vke.core.assets.pipeline.protocols;

import com.vke.api.assets.Protocols;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.pipeline.PipelineData;
import com.vke.api.rendering.abstraction.RenderDevice;
import com.vke.api.rendering.abstraction.Renderer;
import com.vke.api.rendering.abstraction.pipeline.GraphicsPipeline;
import com.vke.core.Context;
import com.vke.core.EngineCreateInfo;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.Op;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.apis.AssetUri;
import com.vke.core.assets.pipeline.stages.PipelineStage;
import com.vke.utils.Utils;
import com.vke.utils.io.Identifier;

public class RenderPipelineProtocol implements AssetProtocol<GraphicsPipeline> {
    @Override
    public String getProtocolName() {
        return Protocols.RENDERPIPELINE;
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

    public static class RenderPipelineLoader implements Loader {

        @Override
        public AssetData load(Context context, Identifier identifier, PipelineStage.ExecutionTarget executionTarget) throws AssetException {
            if (!executionTarget.isUsable(PipelineStage.ExecutionTarget.Main)) return null;

            return Utils.chainExceptions(() -> {
                PipelineData data = PipelineData.fromConfig(ConfigDocument.parseIdentifier(identifier));

                EngineCreateInfo.RendererType rendererType = context.getEngine().rendererType();
                Renderer renderer = context.service(rendererType.serviceName);
                RenderDevice device = renderer.getDevice();

                return new AssetData(Protocols.RENDERPIPELINE, device.createRenderPipeline(data));
            });
        }
    }
}
