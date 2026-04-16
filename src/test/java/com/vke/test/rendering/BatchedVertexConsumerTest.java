package com.vke.test.rendering;

import com.vke.api.assets.r.R;
import com.vke.api.draw.VertexConsumer;
import com.vke.api.draw.shape.ShapeRendererVertex;
import com.vke.api.rendering.abstraction.pipeline.RenderPipeline;
import com.vke.api.rendering.vulkan.pushconstants.PushConstantHandle;
import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.assets.handles.utils.LazyAssetHandle;
import com.vke.core.rendering.draw.DrawContext;
import com.vke.core.services.Services;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.core.vulkan.pipeline.VulkanRenderPipeline;
import com.vke.core.vulkan.vertexconsumer.BatchedVKVertexConsumer;
import com.vke.utils.io.Identifier;
import org.joml.Matrix4f;

public class BatchedVertexConsumerTest extends Scene {

    public BatchedVertexConsumerTest(Identifier name, Context context) {
        super(name, context);
    }

    private LazyAssetHandle<RenderPipeline> PL = R.pipelines.get("batched_consumer_test.pipeline.json");
    private VulkanRenderPipeline pipeline;

    private PushConstantHandle proj, transform;

    private VertexConsumer<ShapeRendererVertex> consumer;

    @Override
    public void onLoad() {
        pipeline = (VulkanRenderPipeline) PL.assume(context);

        proj = pipeline.resolvePushConstant("world");
        transform = pipeline.resolvePushConstant("translation");

        this.consumer = new BatchedVKVertexConsumer<>(this.context, this.context.service(Services.VULKAN_RENDERER),
                new ShapeRendererVertex(0, 0, 0, 0, 0, 0, 0, 0, 0, null), PL, "textures");
    }

    @Override
    public void onDraw(DrawContext ctx) {
        // Draw
        Matrix4f mat = new Matrix4f();
        mat.setOrtho(0, ctx.getWindow().getSize().width(), 0, ctx.getWindow().getSize().height(), 0, 1000, true);
        proj.write(slice -> slice.putMat4(mat));
        transform.write(slice -> slice.putMat4(new Matrix4f()));

        ctx.getCommandBuffer().setPushConstants(PL);
        consumer.draw(ctx);
        //System.exit(0);

    }

    @Override
    public void onUnload() {
        this.consumer.free();
    }

    @Override
    public void free() {

    }

}
