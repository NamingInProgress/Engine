package com.vke.core.rendering.pipeline.driver;

import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.abstraction.pipeline.Pipeline;
import com.vke.api.rendering.abstraction.pipeline.PipelineDriver;
import com.vke.core.rendering.draw.FrameContext;

public class ShapePipelineDriver extends PipelineDriver {
    public ShapePipelineDriver(AssetHandle<? extends Pipeline> pipeline) {
        super(pipeline);
    }

    @Override
    public void use(FrameContext context) {
        bind(context);
        bindDescriptorSets(context);
    }
}
