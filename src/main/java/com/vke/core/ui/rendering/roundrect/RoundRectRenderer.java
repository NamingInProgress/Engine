package com.vke.core.ui.rendering.roundrect;

import com.vke.api.assets.r.R;
import com.vke.api.draw.AbstractStatefulRenderer;
import com.vke.api.draw.Drawable;
import com.vke.api.draw.VertexConsumer;
import com.vke.api.rendering.abstraction.pipeline.RenderPipeline;
import com.vke.api.rendering.vulkan.pushconstants.PushConstantHandle;
import com.vke.core.Context;
import com.vke.core.assets.handles.utils.LazyAssetHandle;
import com.vke.core.draw.ShapeRenderer;
import com.vke.core.rendering.draw.DrawContext;
import com.vke.core.services2.Services;
import com.vke.core.vulkan.pipeline.VulkanRenderPipeline;
import com.vke.core.vulkan.service.VulkanRenderer;
import com.vke.core.vulkan.vertexconsumer.BatchedVKVertexConsumer;
import com.vke.core.window.Window;
import com.vke.utils.io.Disposable;
import org.joml.Matrix4f;

public class RoundRectRenderer extends AbstractStatefulRenderer implements Disposable {
    private final VertexConsumer<RoundRectVertex> vertexConsumer;
    private final LazyAssetHandle<RenderPipeline> PL = R.pipelines.get("roundrect.pipeline.json");
    private final VulkanRenderPipeline pipeline;
    private final PushConstantHandle world, translation;

    private float currHalfWidth, currHalfHeight;
    private float currCenterX, currCenterY;
    private float currRadiusX, currRadiusY;

    public RoundRectRenderer(Context context) {
        this.pipeline = (VulkanRenderPipeline) PL.assume(context);
        this.world = pipeline.resolvePushConstant("world");
        this.translation = pipeline.resolvePushConstant("translation");

        VulkanRenderer renderer = context.service(Services.VULKAN_RENDERER).assumeImplementation();
        this.vertexConsumer = new BatchedVKVertexConsumer<>(context, renderer, RoundRectVertex.EMPTY, PL, "textures");
    }

    public void beginFrame(DrawContext ctx) {
        //first set matrices
        Matrix4f mat = new Matrix4f();
        Window.WindowSize size = ctx.getWindow().getSize();
        mat.setOrtho(0, size.width(), 0, size.height(), 0, 1000, true);
        world.write(slice -> slice.putMat4(mat));
        translation.write(slice -> slice.putMat4(new Matrix4f()));

        ctx.getCommandBuffer().setPushConstants(PL);

        this.vertexConsumer.beginFrame();
    }

    @Override
    public void draw(DrawContext ctx) {
        this.vertexConsumer.draw(ctx);
    }

    private float[] uvwh() {
        if (texture == null) {
            return ShapeRenderer.DEFAULT_UV;
        } else {
            return texture.uvFor();
        }
    }

    private RoundRectVertex v(float x, float y, float u, float v, float strokeWidth) {
        return new RoundRectVertex(
                x, y, z,
                r, g, b, a,
                u, v,
                t,
                currCenterX, currCenterY,
                currHalfWidth,
                currHalfHeight,
                currRadiusX,
                currRadiusY,
                strokeWidth
        );
    }

    public void roundRect(int x, int y, int w, int h, int radX, int radY) {
        currHalfWidth = (float) w / 2;
        currHalfHeight = (float) h / 2;
        currCenterX = x + currHalfWidth;
        currCenterY = y + currHalfHeight;
        currRadiusX = radX;
        currRadiusY = radY;

        float[] uvwh = uvwh();
        float u = uvwh[0]; float v = uvwh[1]; float tw = uvwh[2]; float th = uvwh[3];
        vertexConsumer.begin();
        vertexConsumer.vertices(
                v(x, y, u, v, 0),
                v(x, y + h, u, v + th, 0),
                v(x + w, y + h, u + tw, v + th, 0),
                v(x + w, y, u + tw, v, 0)
        );
        vertexConsumer.indices(0, 1, 2, 2, 3, 0);
        texture(null);
    }

    public void strokeRoundRect(int x, int y, int w, int h, int radX, int radY, int strokeWidth) {
        currHalfWidth = (float) w / 2;
        currHalfHeight = (float) h / 2;
        currCenterX = x + currHalfWidth;
        currCenterY = y + currHalfHeight;
        currRadiusX = radX;
        currRadiusY = radY;

        float[] uvwh = uvwh();
        float u = uvwh[0]; float v = uvwh[1]; float tw = uvwh[2]; float th = uvwh[3];
        vertexConsumer.begin();
        vertexConsumer.vertices(
                v(x, y, u, v, strokeWidth),
                v(x, y + h, u, v + th, strokeWidth),
                v(x + w, y + h, u + tw, v + th, strokeWidth),
                v(x + w, y, u + tw, v, strokeWidth)
        );
        vertexConsumer.indices(0, 1, 2, 2, 3, 0);
        texture(null);
    }

    @Override
    public void free() {
        vertexConsumer.free();
    }
}
