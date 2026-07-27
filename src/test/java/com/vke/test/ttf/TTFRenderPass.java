package com.vke.test.ttf;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.pipeline.RenderPipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.BufferResource;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.ValueResource;
import com.vke.api.rendering.abstraction.rendergraph.RenderPass;
import com.vke.core.draw.ShapeRenderer;
import com.vke.core.draw.ShapeRendererVertex;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.rendering.graph.RenderPassInstance;
import org.joml.Matrix4f;

import java.io.IOException;
import java.util.List;

public class TTFRenderPass extends RenderPass {

    private AssetHandle<RenderPipeline> pip, pip2;
    private BufferResource matrixStack, mxs2;
    private ValueResource proj, proj2;

    public TTFRenderPass(RenderSystem renderSystem, RenderPassInstance instance) {
        super(renderSystem, instance);
    }

    @Override
    public void onLoad() {
        try {
            this.pip = R.pipelines.get("vke:batched_consumer_test.pipeline.json");
            this.pip2 = R.pipelines.get("vke:batched_consumer_test_2.pipeline.json");

            RenderPipeline p = pip.acquire(renderSystem);
            RenderPipeline p2 = pip2.acquire(renderSystem);

            this.matrixStack = p.resource("matrixStack");
            this.proj = p.resource("world");

            this.mxs2 = p2.resource("matrixStack");
            this.proj2 = p2.resource("world");

            this.proj.write(s -> s.putMat4(new Matrix4f().ortho(0, 800, 0, 600, 0, 1000, true)));
            this.proj2.write(s -> s.putMat4(new Matrix4f().ortho(0, 800, 0, 600, 0, 1000, true)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execute(CommandBuffer cmd, GraphContext context) {
        Texture colorOut = instance.getOutputTexture("colorOut");

        cmd.beginRendering(new CommandBuffer.RenderingInfo(List.of(CommandBuffer.AttachmentInfo.color(colorOut)), null));

        ShapeRenderer<ShapeRendererVertex> sr = context.get("sr");
        cmd.bindPipeline(pip);
        this.matrixStack.write(w -> sr.getMatrixStack().upload(w));
        cmd.bindDescriptorSets(pip);
        cmd.setPushConstants(pip);

        sr.draw();

        cmd.bindPipeline(pip2);
        this.mxs2.write(w -> sr.getMatrixStack().upload(w));
        cmd.bindDescriptorSets(pip2);
        cmd.setPushConstants(pip2);

        context.<VertexConsumer<ShapeRendererVertex>>get("vc").draw();

        cmd.endRendering();
    }
}
