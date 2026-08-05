package com.vke.impl.pass;

import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.rendergraph.RenderPass;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.rendering.graph.RenderPassInstance;
import com.vke.core.rendering.pipeline.RenderPipelines;
import com.vke.impl.driver.FullScreenDriver;
import com.vke.impl.vertex.FullscreenQuadVertex;
import com.vke.utils.DrawUtils;

public class ITSRenderPass extends RenderPass {

    private VertexConsumer<FullscreenQuadVertex> vc;

    public ITSRenderPass(RenderSystem renderSystem, RenderPassInstance instance) {
        super(renderSystem, instance);
    }

    @Override
    public void onLoad() {
        this.vc = renderSystem.renderer().getVertexConsumerProvider().get(FullscreenQuadVertex.TEMPLATE);
    }

    @Override
    public void execute(CommandBuffer cmd, GraphContext context) {
        cmd.beginRendering(new CommandBuffer.RenderingInfo(CommandBuffer.AttachmentInfo.color(instance.getOutputTexture("colorOut"))));

        RenderPipelines.FULL_SCREEN.texture(instance.getInputTexture("colorIn"));
        RenderPipelines.FULL_SCREEN.use();

        DrawUtils.fullscreenQuad(vc);
        vc.draw();

        cmd.endRendering();
    }

}
