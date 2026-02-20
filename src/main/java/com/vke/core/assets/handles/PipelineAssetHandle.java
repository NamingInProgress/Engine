package com.vke.core.assets.handles;

import com.vke.api.assets.AssetHandle;
import com.vke.api.vulkan.pipeline.RenderPipeline;
import com.vke.core.VKEngine;

import java.io.IOException;

public class PipelineAssetHandle implements AssetHandle<RenderPipeline> {

    private RenderPipeline pipeline;

    @Override
    public Type getType() {
        return Type.Pipeline;
    }

    @Override
    public RenderPipeline acquire(VKEngine engine) throws IOException {
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
