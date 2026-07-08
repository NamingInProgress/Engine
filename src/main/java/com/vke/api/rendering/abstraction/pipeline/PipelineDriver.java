package com.vke.api.rendering.abstraction.pipeline;

import com.vke.api.assets.AssetHandle;
import com.vke.core.rendering.draw.FrameContext;

public abstract class PipelineDriver {

    public static class DefaultPipelineDriver extends PipelineDriver {

        public DefaultPipelineDriver(AssetHandle<? extends Pipeline> pipeline) {
            super(pipeline);
        }

        @Override
        public void use(FrameContext context) {
            bind(context);
            bindDescriptorSets(context);
            bindPushConstants(context);
        }

    }

    private final AssetHandle<? extends Pipeline> pipeline;

    public PipelineDriver(AssetHandle<? extends Pipeline> pipeline) {
        this.pipeline = pipeline;
    }

    public void bind(FrameContext ctx) {
        ctx.getCommandBuffer().bindPipeline(pipeline);
    }

    public void bindDescriptorSets(FrameContext ctx) {
        ctx.getCommandBuffer().bindDescriptorSets(pipeline);
    }

    public void bindPushConstants(FrameContext ctx) {
        ctx.getCommandBuffer().setPushConstants(pipeline);
    }

    public abstract void use(FrameContext context);

}
