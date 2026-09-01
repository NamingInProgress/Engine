package com.vke.core.rendering.passes;

import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.rendergraph.RenderPass;
import com.vke.core.color.OldColor;
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
        Texture gbuf_material_idx = instance.getOutputTexture("gbuf_material_idx");
        Texture gbuf_mesh_uvs = instance.getOutputTexture("gbuf_mesh_uvs");

        Texture depthOut = instance.getOutputTexture("depthOut");

        this.beginRendering(cmd, List.of("gbuf_normal", "gbuf_material_idx", "gbuf_mesh_uvs"), "depthOut", List.of(OldColor.BLACK, new OldColor(-1, -1, -1, -1), OldColor.BLACK), OldColor.WHITE);

        int inst = context.get("inst");
        RenderPipelines.DEFERRED.setLocal(context.get("mats"));
        RenderPipelines.DEFERRED.use();
        DemoScene.MESH.drawInstanced(inst);

        cmd.endRendering();

        this.beginRendering(cmd, List.of("colorOut"), OldColor.BLACK);

        gbuf_normal.useInShader();
        gbuf_material_idx.useInShader();
        gbuf_mesh_uvs.useInShader();
        depthOut.useInShader();

        RenderPipelines.DEFERRED_LIGHT_PASS.set(gbuf_normal, gbuf_material_idx, gbuf_mesh_uvs, depthOut);
        RenderPipelines.DEFERRED_LIGHT_PASS.use();

        DrawUtils.fullscreenQuad(vc);

        vc.draw();

        cmd.endRendering();
    }

}
