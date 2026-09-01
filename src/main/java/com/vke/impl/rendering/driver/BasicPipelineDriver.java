package com.vke.impl.rendering.driver;

import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.pipeline.Pipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.PipelineDriver;

public class BasicPipelineDriver extends PipelineDriver {

    public BasicPipelineDriver(RenderSystem sys, AssetHandle<? extends Pipeline> pipeline) {
        super(sys, pipeline);
    }

    @Override
    public void use() {
        bind();
        bindDescriptorSets();
        bindPushConstants();
    }
}
