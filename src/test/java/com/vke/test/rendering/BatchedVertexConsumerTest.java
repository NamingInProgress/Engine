package com.vke.test.rendering;

import com.vke.api.assets.r.R;
import com.vke.api.draw.VertexConsumer;
import com.vke.api.draw.VertexFactory;
import com.vke.core.draw.ShapeRenderer;
import com.vke.core.draw.ShapeRendererVertex;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.pipeline.RenderPipeline;
import com.vke.api.rendering.vulkan.pushconstants.PushConstantHandle;
import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.assets.handles.utils.LazyAssetHandle;
import com.vke.core.rendering.draw.DrawContext;
import com.vke.core.services.Services;
import com.vke.core.spline.Spline;
import com.vke.core.spline.SplineRenderer;
import com.vke.core.vulkan.pipeline.VulkanRenderPipeline;
import com.vke.core.vulkan.vertexconsumer.BatchedVKVertexConsumer;
import com.vke.utils.io.Identifier;
import org.joml.Matrix4f;

import java.util.Random;

public class BatchedVertexConsumerTest extends Scene {

    public BatchedVertexConsumerTest(Identifier name, Context context) {
        super(name, context);
    }

    private LazyAssetHandle<RenderPipeline> PL = R.pipelines.get("batched_consumer_test.pipeline.json");
    private VulkanRenderPipeline pipeline;
    private Texture scaryVK;
    private Texture missing;
    private Texture bear_performance;

    private PushConstantHandle proj, transform;

    private VertexConsumer<ShapeRendererVertex> consumer;
    private ShapeRenderer<ShapeRendererVertex> shapeRenderer;
    private SplineRenderer<ShapeRendererVertex> splineRenderer;
    private Spline spline;

    @Override
    public void onLoad() {
        pipeline = (VulkanRenderPipeline) PL.assume(context);

        proj = pipeline.resolvePushConstant("world");
        transform = pipeline.resolvePushConstant("translation");

        this.consumer = new BatchedVKVertexConsumer<>(this.context, this.context.service(Services.VULKAN_RENDERER),
                new ShapeRendererVertex(0, 0, 0, 0, 0, 0, 0, 0, 0, null), PL, "textures");
        this.shapeRenderer = new ShapeRenderer<>(consumer, VertexFactory.DEFAULT);
        this.splineRenderer = new SplineRenderer<>(consumer, VertexFactory.DEFAULT);

        this.scaryVK = R.textures.get("scaryvulkan.png").assume(context);
        this.missing = R.textures.get("missing.png").assume(context);
        this.bear_performance = R.textures.get("bear_performance.png").assume(context);

        this.spline = new Spline()
                .moveTo(100, 100)
                .lineTo(150, 100)
                .cubicTo(250, 200, 150, 100, 200, 200)
                .cubicTo(300, 50, 400, 200, 350, 100)
                .lineTo(400, 50)
                .cubicTo(250, 300, 500, 100, 500, 700)
                .quadTo(200, 400, 250, 400)
                .quadTo(100, 100, 150, 420)

                .moveTo(300, 300)
                .quadTo(350, 350, 300, 350)
                .quadTo(400, 300, 400, 350)
                .quadTo(350, 280, 400, 280)
                .quadTo(300, 300, 300, 280)
                .close();
    }

    @Override
    public void onDraw(DrawContext ctx) {
        // Draw
        Matrix4f mat = new Matrix4f();
        mat.setOrtho(0, ctx.getWindow().getSize().width(), 0, ctx.getWindow().getSize().height(), 0, 1000, true);
        proj.write(slice -> slice.putMat4(mat));
        transform.write(slice -> slice.putMat4(new Matrix4f()));

        ctx.getCommandBuffer().setPushConstants(PL);
        consumer.beginFrame();

        splineRenderer.drawSpline(null, spline, null, 0.5f);
        splineRenderer.draw(ctx);

        //shapeRenderer.texture(scaryVK);
        //shapeRenderer.color(0, 0, 0, 1);
        //shapeRenderer.circle(400, 300, 100, 50);
//
        //shapeRenderer.color(1, 0, 0, 1);
        //shapeRenderer.rect(100, 100, 100, 100);
//
        //shapeRenderer.texture(missing);
        //shapeRenderer.color(0, 0, 0, 1);
        //shapeRenderer.ovalArc(200, 200, 100, 50, 0, 90, 30);
//
//
//
        //shapeRenderer.draw(ctx);

//        consumer.begin();
//        consumer.vertices(new ShapeRendererVertex(100, 100, 0, 0, 0, 0, 1, 0, 1, scaryVK));
//        consumer.vertices(new ShapeRendererVertex(100, 200, 0, 0, 0, 0, 1, 0, 0, scaryVK));
//        consumer.vertices(new ShapeRendererVertex(200, 200, 0, 0, 0, 0, 1, 1, 0, scaryVK));
//        consumer.vertices(new ShapeRendererVertex(200, 100, 0, 0, 0, 0, 1, 1, 1, scaryVK));
//        consumer.indices(0, 1, 2, 2, 3, 0);
//
//        consumer.begin();
//        consumer.vertices(new ShapeRendererVertex(200, 100, 0, 0, 0, 0, 1, 0, 1, missing));
//        consumer.vertices(new ShapeRendererVertex(200, 200, 0, 0, 0, 0, 1, 0, 0, missing));
//        consumer.vertices(new ShapeRendererVertex(300, 200, 0, 0, 0, 0, 1, 1, 0, missing));
//        consumer.vertices(new ShapeRendererVertex(300, 100, 0, 0, 0, 0, 1, 1, 1, missing));
//        consumer.indices(0, 1, 2, 2, 3, 0);
//        consumer.draw(ctx);
//
//        consumer.begin();
//        consumer.vertices(new ShapeRendererVertex(300, 100, 0, 0, 0, 0, 1, 0, 1, bear_performance));
//        consumer.vertices(new ShapeRendererVertex(300, 200, 0, 0, 0, 0, 1, 0, 0, bear_performance));
//        consumer.vertices(new ShapeRendererVertex(400, 200, 0, 0, 0, 0, 1, 1, 0, bear_performance));
//        consumer.vertices(new ShapeRendererVertex(400, 100, 0, 0, 0, 0, 1, 1, 1, bear_performance));
//        consumer.indices(0, 1, 2, 2, 3, 0);

        //System.exit(0);

    }

    @Override
    public void onUnload() {
    }

    @Override
    public void free() {
        this.consumer.free();
    }

}
