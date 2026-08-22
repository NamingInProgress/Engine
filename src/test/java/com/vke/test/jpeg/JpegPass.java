package com.vke.test.jpeg;

import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.LoadOp;
import com.vke.api.rendering.abstraction.renderer.enums.StoreOp;
import com.vke.api.rendering.abstraction.rendergraph.RenderPass;
import com.vke.core.draw.ShapeRenderer;
import com.vke.core.draw.ShapeRendererVertex;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.rendering.graph.RenderPassInstance;
import com.vke.core.rendering.pipeline.RenderPipelines;

import java.util.List;

public class JpegPass extends RenderPass {
    private ShapeRenderer<ShapeRendererVertex> sr;

    public JpegPass(RenderSystem renderSystem, RenderPassInstance instance) {
        super(renderSystem, instance);
    }

    @Override
    public void onLoad() {
        var consumer = renderSystem.renderer().getVertexConsumerProvider().get(ShapeRendererVertex.TEMPLATE);
        this.sr = new ShapeRenderer<>(consumer, ShapeRendererVertex.FACTORY);
    }

    @Override
    public void execute(CommandBuffer cmd, GraphContext context) {
        AssetHandle<Texture> texture = context.get("tex");

        Texture output = instance.getOutputTexture("colorOut");
        cmd.beginRendering(new CommandBuffer.RenderingInfo(
                new CommandBuffer.AttachmentInfo(output, LoadOp.CLEAR, StoreOp.STORE),
                null
        ));
        sr.color(1, 0, 0, 1);
        sr.rect(100, 100, 100, 100);

        RenderPipelines.SHAPE.setMatrices(sr.getMatrixStack());
        RenderPipelines.SHAPE.use();

        sr.draw();
        cmd.endRendering();
    }
}
