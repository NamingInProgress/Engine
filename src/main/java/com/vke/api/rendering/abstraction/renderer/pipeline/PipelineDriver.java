package com.vke.api.rendering.abstraction.renderer.pipeline;

import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.utils.exception.Unreachable;

import java.io.IOException;

public abstract class PipelineDriver {

    protected final AssetHandle<? extends Pipeline> pipeline;
    protected final RenderSystem sys;
    protected final Pipeline p;

    public PipelineDriver(RenderSystem sys, AssetHandle<? extends Pipeline> pipeline) {
        this.pipeline = pipeline;
        this.sys = sys;
        try {
            this.p = pipeline.acquire(sys);
        } catch (IOException e) {
            sys.throwException(e, "PipelineDriver");
            throw new Unreachable("how the fuck did you get here");
        }
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
