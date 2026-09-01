package com.vke.core.assets.pipeline.protocols.shader;

import com.vke.api.assets.Protocols;
import com.vke.api.assets.anot.Protocol;
import com.vke.api.rendering.abstraction.renderer.RenderDevice;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.rendering.abstraction.renderer.enums.ShaderType;
import com.vke.api.rendering.abstraction.renderer.shader.Shader;
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

@Protocol
public class VertexShaderProtocol implements AssetProtocol<Shader> {

    @Override
    public String getProtocolName() {
        return Protocols.VERTSHADER;
    }

    @Override
    public AssetData getField(AssetData data, AssetUri uri) throws AssetException {
        throw AssetException.unknownSelector(getProtocolName(), uri.getSelector());
    }

    @Override
    public Loader getLoader() {
        return new VertexShaderLoader();
    }

    @Override
    public boolean applies(AssetData a, AssetData b, Op op) {
        return false;
    }

    public static class VertexShaderLoader implements Loader {
        @Override
        public AssetData load(Context context, FileIdentifier identifier, PipelineStage.ExecutionTarget executionTarget) throws AssetException {
            if (!executionTarget.isUsable(PipelineStage.ExecutionTarget.Main)) return null;

            EngineCreateInfo.RendererType rendererType = context.getEngine().rendererType();
            Renderer renderer = context.service(rendererType.serviceName);
            RenderDevice device = renderer.getDevice();

            return Utils.chainExceptions(() ->
                    new AssetData(Protocols.VERTSHADER, device.createShader(identifier, ShaderType.VERTEX))
            );
        }
    }
}
