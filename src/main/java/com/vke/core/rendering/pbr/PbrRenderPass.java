package com.vke.core.rendering.pbr;

import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.pipeline.RenderPipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldArrayResource;
import com.vke.api.rendering.abstraction.rendergraph.RenderPass;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.rendering.graph.RenderPassInstance;

import java.io.IOException;

public class PbrRenderPass extends RenderPass {

    protected final AssetHandle<RenderPipeline> pipelineHandle;
    protected RenderPipeline pipeline;
    protected FieldArrayResource ssbo_mat;

    public PbrRenderPass(RenderSystem renderSystem, RenderPassInstance instance, AssetHandle<RenderPipeline> pipeline) {
        super(renderSystem, instance);
        this.pipelineHandle = pipeline;
    }

    @Override
    public void onLoad() {
        try {
            this.pipeline = pipelineHandle.acquire(renderSystem);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ssbo_mat = this.pipeline.resource("u_MaterialBuffer");
    }

    @Override
    public void execute(CommandBuffer cmd, GraphContext context) {

    }

}
