package com.vke.core.assets.pipeline.protocols.shader;

import com.vke.api.assets.Protocols;
import com.vke.api.rendering.abstraction.RenderDevice;
import com.vke.api.rendering.abstraction.Renderer;
import com.vke.api.rendering.abstraction.enums.ShaderType;
import com.vke.api.rendering.abstraction.shader.Shader;
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

public class ComputeShaderProtocol implements AssetProtocol<Shader> {

    @Override
    public String getProtocolName() {
        return Protocols.COMPSHADER;
    }

    @Override
    public AssetData getField(AssetData data, AssetUri uri) throws AssetException {
        throw AssetException.unknownSelector(getProtocolName(), uri.getSelector());
    }

    @Override
    public Loader getLoader() {
        return new ComputeShaderLoader();
    }

    @Override
    public boolean applies(AssetData a, AssetData b, Op op) {
        return false;
    }

    public static class ComputeShaderLoader implements Loader {
        @Override
        public AssetData load(Context context, Identifier identifier, PipelineStage.ExecutionTarget executionTarget) throws AssetException {
            if (!executionTarget.isUsable(PipelineStage.ExecutionTarget.Main)) return null;

            EngineCreateInfo.RendererType rendererType = context.getEngine().rendererType();
            Renderer renderer = context.service(rendererType.serviceName);
            RenderDevice device = renderer.getDevice();

            return Utils.chainExceptions(() ->
                    new AssetData(Protocols.COMPSHADER, device.createShader(identifier, ShaderType.COMPUTE))
            );
        }
    }
}
