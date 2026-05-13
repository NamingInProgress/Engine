package com.vke.core.ui.rendering.core.passes;

import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.pipeline.RenderPipeline;
import com.vke.api.rendering.vulkan.descriptors.handles.array.EntryArrayHandle;
import com.vke.api.rendering.vulkan.pushconstants.PushConstantHandle;
import com.vke.core.VKEngine;
import com.vke.core.assets.handles.utils.LazyAssetHandle;
import com.vke.core.rendering.draw.DrawContext;
import com.vke.core.ui.geom.Fect;
import com.vke.core.ui.rendering.core.GeneralDrawRequest;
import com.vke.core.ui.rendering.core.UiGeneralVertex;
import com.vke.core.vulkan.pipeline.VulkanRenderPipeline;
import com.vke.core.vulkan.service.VulkanRenderer;
import com.vke.core.vulkan.vertexconsumer.FastVertexConsumer;
import com.vke.core.window.Window;
import org.joml.Matrix4f;

public class GeneralRenderPass {
    private final static LazyAssetHandle<RenderPipeline> PL = R.pipelines.get("uicore-general.pipeline.json");

    private final VKEngine engine;
    private final VulkanRenderer renderer;
    private final FastVertexConsumer<UiGeneralVertex> consumer;

    private final VulkanRenderPipeline pipeline;
    private final PushConstantHandle pc_proj;
    private Matrix4f projMat;

    private final EntryArrayHandle uh_transform;
    private final EntryArrayHandle uh_clip;

    public GeneralRenderPass(VKEngine engine, VulkanRenderer renderer) {
        this.engine = engine;
        this.renderer = renderer;
        consumer = new FastVertexConsumer<>(engine, renderer, UiGeneralVertex.EMPTY);

        this.pipeline = (VulkanRenderPipeline) PL.assume(engine);

        this.pc_proj = pipeline.resolvePushConstant("projection");
        this.projMat = new Matrix4f();

        this.uh_transform = pipeline.resolveUniform("TransformBuffer.matrices");
        this.uh_clip = pipeline.resolveUniform("ClipBuffer.rects");
    }

    public void beginFrame(Matrix4f[] transforms, Fect[] clips) {
        consumer.beginFrame();

        pc_proj.write(slice -> slice.putMat4(projMat));

        for (int i = 0; i < transforms.length; i++) {
            Matrix4f transform = transforms[i];
            uh_transform.write(slice -> {
                slice.putMat4(transform);
            }, i);
        }

        for (int i = 0; i < clips.length; i++) {
            Fect clip = clips[i];
            uh_clip.write(slice -> {
                slice.putFloat4(clip.x, clip.y, clip.w, clip.h);
            }, i);
        }
    }

    public void acceptRequest(GeneralDrawRequest request) {
        UiGeneralVertex[] vertices = request.getVertices();
        int[] indices = request.getIndices();
        consumer.begin();
        consumer.vertices(vertices);
        consumer.indices(indices);
    }

    public void draw(DrawContext context) {
        context.getCommandBuffer().bindPipeline(PL);
        context.getCommandBuffer().setPushConstants(PL);
        context.getCommandBuffer().bindDescriptorSets(PL);

        consumer.draw(context);
    }

    public void onResize(Window.WindowSize size) {
        projMat.setOrtho(0, size.width(), 0, size.height(), 0, 1000, true);
    }
}
