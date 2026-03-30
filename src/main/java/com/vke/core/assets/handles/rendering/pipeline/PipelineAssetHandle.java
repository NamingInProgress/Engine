package com.vke.core.assets.handles.rendering.pipeline;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.Protocols;
import com.vke.core.Context;
import com.vke.api.rendering.vulkan.pipeline.RenderPipeline;
import com.vke.core.VKEngine;

import java.io.IOException;

public class PipelineAssetHandle implements AssetHandle<RenderPipeline> {

    private RenderPipeline pipeline;

    @Override
    public String getProtocol() {
        return Protocols.RENDERPIPELINE;
    }

    @Override
    public RenderPipeline acquire(Context context) throws IOException {
        return null;
    }

    @Override
    public RenderPipeline get() {
        return pipeline;
    }

    @Override
    public boolean isAvailable() {
        return pipeline != null;
    }

    @Override
    public void free() {

    }
}
