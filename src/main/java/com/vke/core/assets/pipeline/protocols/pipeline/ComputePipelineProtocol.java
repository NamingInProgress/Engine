package com.vke.core.assets.pipeline.protocols.pipeline;

import com.vke.api.assets.Protocols;
import com.vke.api.assets.anot.Protocol;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.rendering.abstraction.renderer.RenderDevice;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.rendering.abstraction.renderer.pipeline.ComputePipeline;
import com.vke.api.rendering.vulkan.pipeline.ComputePipelineData;
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

import java.io.IOException;

@Protocol
public class ComputePipelineProtocol implements AssetProtocol<ComputePipeline> {
    @Override
    public String getProtocolName() {
        return Protocols.COMPUTE_PIPELINE;
    }

    @Override
    public AssetData getField(AssetData data, AssetUri uri) throws AssetException {
        throw AssetException.unknownSelector(getProtocolName(), uri.getSelector());
    }

    @Override
    public Loader getLoader() {
        return new ComputePipelineLoader();
    }

    @Override
    public boolean applies(AssetData a, AssetData b, Op op) {
        return false;
    }

    public static AssetData fromConfig(Context context, ConfigDocument configDocument) {
        return Utils.chainExceptions(() -> {
            ComputePipelineData data = ComputePipelineData.fromConfig(configDocument);

            EngineCreateInfo.RendererType rendererType = context.getEngine().rendererType();
            Renderer renderer = context.service(rendererType.serviceName);
            RenderDevice device = renderer.getDevice();

            return new AssetData(Protocols.COMPUTE_PIPELINE, device.createComputePipeline(data));
        });
    }

    public static class ComputePipelineLoader implements Loader {

        @Override
        public AssetData load(Context context, Identifier identifier, PipelineStage.ExecutionTarget executionTarget) throws AssetException {
            if (!executionTarget.isUsable(PipelineStage.ExecutionTarget.Main)) return null;
            try {
                return fromConfig(context, ConfigDocument.parseIdentifier(identifier));
            } catch (IOException e) {
                throw new AssetException(e);
            }
        }
    }
}
