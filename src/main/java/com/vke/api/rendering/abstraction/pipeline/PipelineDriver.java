package com.vke.api.rendering.abstraction.pipeline;

import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.abstraction.RenderSystem;

public abstract class PipelineDriver {

    public static class DefaultPipelineDriver extends PipelineDriver {

        public DefaultPipelineDriver(RenderSystem sys, AssetHandle<? extends Pipeline> pipeline) {
            super(sys, pipeline);
        }

        @Override
        public void use() {
            bind();
            bindDescriptorSets();
            bindPushConstants();
        }

    }

    private final AssetHandle<? extends Pipeline> pipeline;
    private final RenderSystem sys;

    public PipelineDriver(RenderSystem sys, AssetHandle<? extends Pipeline> pipeline) {
        this.pipeline = pipeline;
        this.sys = sys;
    }

    public void bind() {
        sys.getCurrentCommandBuffer().bindPipeline(pipeline);
    }

    public void bindDescriptorSets() {
        sys.getCurrentCommandBuffer().bindDescriptorSets(pipeline);
    }

    public void bindPushConstants() {
        sys.getCurrentCommandBuffer().setPushConstants(pipeline);
    }

    public abstract void use();

}
