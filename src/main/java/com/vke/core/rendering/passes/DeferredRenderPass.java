package com.vke.core.rendering.passes;

import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.rendergraph.RenderPass;
import com.vke.core.color.Color;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.rendering.graph.RenderPassInstance;
import com.vke.core.rendering.pipeline.RenderPipelines;
import com.vke.impl.vertex.FullscreenQuadVertex;
import com.vke.demo.DemoScene;
import com.vke.utils.DrawUtils;

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

        Texture depthOut = instance.getOutputTexture("depthOut");

        this.beginRendering(cmd, List.of("gbuf_normal", "gbuf_albedo_spec"), "depthOut", Color.BLACK, Color.WHITE);

        RenderPipelines.DEFERRED.setLocal(context.get("mats"));
        RenderPipelines.DEFERRED.use();
        DemoScene.MESH.drawInstanced(250);

        cmd.endRendering();

        this.beginRendering(cmd, List.of("colorOut"), Color.BLACK);

        gbuf_normal.useInShader();
        gbuf_albedo_spec.useInShader();
        depthOut.useInShader();

        RenderPipelines.DEFERRED_LIGHT_PASS.set(gbuf_normal, gbuf_albedo_spec, depthOut);
        RenderPipelines.DEFERRED_LIGHT_PASS.use();

        DrawUtils.fullscreenQuad(vc);

        vc.draw();

        cmd.endRendering();
    }

}
