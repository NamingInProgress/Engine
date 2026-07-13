package com.vke.core.rendering.pipeline.driver;

import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.abstraction.RenderSystem;
import com.vke.api.rendering.abstraction.pipeline.Pipeline;
import com.vke.api.rendering.abstraction.pipeline.PipelineDriver;

public class ShapePipelineDriver extends PipelineDriver {

    public ShapePipelineDriver(RenderSystem sys, AssetHandle<? extends Pipeline> pipeline) {
        super(sys, pipeline);
    }

    @Override
    public void use() {
        bind();
        bindDescriptorSets();
    }
}
