package com.vke.core.rendering.passes;

import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.LoadOp;
import com.vke.api.rendering.abstraction.renderer.enums.StoreOp;
import com.vke.api.rendering.abstraction.rendergraph.RenderPass;
import com.vke.core.color.Color;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.rendering.graph.RenderPassInstance;
import com.vke.core.rendering.pipeline.RenderPipelines;
import com.vke.core.rendering.post.FullscreenQuadVertex;
import com.vke.demo.DemoScene;

import java.util.List;

public class DeferredRenderPass extends RenderPass {

    private VertexConsumer<FullscreenQuadVertex> vc;

    public DeferredRenderPass(RenderSystem renderSystem, RenderPassInstance instance) {
        super(renderSystem, instance);
    }

    @Override
    public void onLoad() {
        this.vc = renderSystem.renderer().getVertexConsumerProvider().get(FullscreenQuadVertex.TEMPLATE);
    }

    @Override
    public void execute(CommandBuffer cmd, GraphContext context) {
        Texture gbuf_normal = instance.getOutputTexture("gbuf_normal");
        Texture gbuf_albedo_spec = instance.getOutputTexture("gbuf_albedo_spec");
        Texture gbuf_depth = instance.getOutputTexture("gbuf_depth");

        Texture colorOut = instance.getOutputTexture("colorOut");
        Texture depthOut = instance.getOutputTexture("depthOut");

        cmd.beginRendering(new CommandBuffer.RenderingInfo(
            List.of(
                CommandBuffer.AttachmentInfo.color(gbuf_normal, Color.BLACK),
                CommandBuffer.AttachmentInfo.color(gbuf_albedo_spec, Color.BLACK)
            ),
            CommandBuffer.AttachmentInfo.depth(gbuf_depth))
        );

        RenderPipelines.DEFERRED.setLocal(context.get("mats"));
        RenderPipelines.DEFERRED.use();
        DemoScene.MESH.drawInstanced(1000);

        cmd.endRendering();

        cmd.beginRendering(new CommandBuffer.RenderingInfo(
                List.of(
                        CommandBuffer.AttachmentInfo.color(colorOut, Color.BLACK)
                ),
                null)
        );

        gbuf_normal.useInShader();
        gbuf_albedo_spec.useInShader();
        gbuf_depth.useInShader();

        RenderPipelines.DEFERRED_LIGHT_PASS.set(gbuf_normal, gbuf_albedo_spec, gbuf_depth);
        RenderPipelines.DEFERRED_LIGHT_PASS.use();

        vc.vertices(new FullscreenQuadVertex(-1.0f, -1.0f, 0.0f, 0.0f)); // 0
        vc.vertices(new FullscreenQuadVertex( 1.0f, -1.0f, 1.0f, 0.0f)); // 1
        vc.vertices(new FullscreenQuadVertex( 1.0f,  1.0f, 1.0f, 1.0f)); // 2
        vc.vertices(new FullscreenQuadVertex(-1.0f,  1.0f, 0.0f, 1.0f)); // 3
        vc.indices(0, 1, 2, 0, 2, 3);

        vc.draw();

        cmd.endRendering();

        if (depthOut != null) {
            cmd.copyImageToImage(gbuf_depth, depthOut, 0, 0, 0, 0);
        }
    }

}
