package com.vke.api.rendering.abstraction.renderer.commands;

import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.abstraction.renderer.data.GpuBuffer;
import com.vke.api.rendering.abstraction.renderer.data.ImageView;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.LoadOp;
import com.vke.api.rendering.abstraction.renderer.enums.StoreOp;
import com.vke.api.rendering.abstraction.renderer.pipeline.Pipeline;
import com.vke.api.rendering.abstraction.renderer.sync.Fence;
import com.vke.api.rendering.abstraction.renderer.sync.Semaphore;
import com.vke.api.rendering.vulkan.ImageLayout;
import com.vke.core.color.Color;
import com.vke.core.geometry.Rect;
import com.vke.core.rendering.vulkan.Scissor;
import com.vke.core.rendering.vulkan.Viewport;
import com.vke.api.rendering.abstraction.renderer.enums.QueueType;
import com.vke.utils.io.Disposable;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public interface CommandBuffer extends Disposable {

    class SubmitInfo {

        private final QueueType type;
        private final boolean immediate;
        private final Semaphore image, present;
        private final Fence fence;

        public SubmitInfo(Semaphore imageSemaphore, Semaphore presentSemaphore, Fence fence, QueueType queueType, boolean immediate) {
            this.type = queueType;
            this.immediate = immediate;
            this.image = imageSemaphore;
            this.present = presentSemaphore;
            this.fence = fence;
        }

        public static SubmitInfo immediate(Fence fence) {
            return new SubmitInfo(null, null, fence, QueueType.TRANSFER, true);
        }

        public QueueType getType() {
            return type;
        }

        public boolean isImmediate() {
            return immediate;
        }

        public Semaphore getImageSemaphore() {
            return image;
        }

        public Semaphore getPresentSemaphore() {
            return present;
        }

        public Fence getFence() {
            return fence;
        }

    }

    record RenderingInfo(List<AttachmentInfo> colorAttachments,
                         @Nullable AttachmentInfo depthAttachment,
                         @Nullable AttachmentInfo stencilAttachment,
                         Rect renderArea) {
        public RenderingInfo(@Nullable AttachmentInfo... colorAttachments) {
            this(Arrays.stream(colorAttachments).toList(), null, null, null);
        }

        public RenderingInfo(@Nullable List<AttachmentInfo> colorAttachments, @Nullable AttachmentInfo depthAttachment) {
            this(colorAttachments, depthAttachment, null, null);
        }

        public RenderingInfo(@Nullable List<AttachmentInfo> colorAttachments, @Nullable AttachmentInfo depthAttachment,
                             @Nullable AttachmentInfo stencilAttachment) {
            this(colorAttachments, depthAttachment, stencilAttachment, null);
        }

        public RenderingInfo(@Nullable AttachmentInfo colorAttachment, @Nullable AttachmentInfo depthAttachment) {
            this(colorAttachment == null ? null : List.of(colorAttachment), depthAttachment, null, null);
        }

        public RenderingInfo(@Nullable AttachmentInfo colorAttachment, @Nullable AttachmentInfo depthAttachment,
                             @Nullable AttachmentInfo stencilAttachment) {
            this(colorAttachment == null ? null : List.of(colorAttachment), depthAttachment, stencilAttachment, null);
        }
    }

    record AttachmentInfo(Texture tex, ImageView view, LoadOp loadOp, StoreOp storeOp,
                          float[] clearColor) {
        public AttachmentInfo(Texture tex, ImageView view, LoadOp loadOp,
                              StoreOp storeOp) {
            this(tex, view, loadOp, storeOp, new float[4]);
        }

        public AttachmentInfo(Texture tex, LoadOp loadOp,
                              StoreOp storeOp) {
            this(tex, tex.defaultView(), loadOp, storeOp, new float[4]);
        }

        public AttachmentInfo(Texture tex, LoadOp loadOp,
                              StoreOp storeOp, float[] clearColor) {
            this(tex, tex.defaultView(), loadOp, storeOp, clearColor);
        }

        public static AttachmentInfo color(Texture tex, Color clearColor) {
            return new AttachmentInfo(tex, LoadOp.CLEAR, StoreOp.STORE, clearColor.toFloat());
        }

        public static AttachmentInfo color(Texture tex) {
            return new AttachmentInfo(tex, LoadOp.CLEAR, StoreOp.STORE, Color.VKE.toFloat());
        }

        public static AttachmentInfo depth(Texture tex, float clear) {
            return new AttachmentInfo(tex, LoadOp.CLEAR, StoreOp.STORE, new float[]{ clear });
        }

        public static AttachmentInfo depth(Texture tex) {
            return new AttachmentInfo(tex, LoadOp.CLEAR, StoreOp.STORE, new float[]{ 1.0f });
        }

        public static AttachmentInfo stencil(Texture tex, float clear) {
            return new AttachmentInfo(tex, LoadOp.CLEAR, StoreOp.STORE, new float[]{ clear });
        }

        public static AttachmentInfo stencil(Texture tex) {
            return new AttachmentInfo(tex, LoadOp.CLEAR, StoreOp.STORE, new float[]{ 0.0f });
        }
    }

    boolean isRecording();

    void begin();
    void beginRendering();
    void beginRendering(RenderingInfo info);
    void end();
    void endRendering();
    void reset();

    void bindPipeline(AssetHandle<? extends Pipeline> pipeline);

    void setPushConstants(AssetHandle<? extends Pipeline> pipeline);
    void bindDescriptorSets(AssetHandle<? extends Pipeline> pipeline);

    void setViewport(Viewport viewport);
    void setScissor(Scissor scissor);

    void draw(int vertexCount, int instanceCount, int firstVertex, int firstInstance);
    void drawIndexed(int indexCount, int instanceCount, int firstIndex, int vertexOffset, int firstInstance);

    void copyBufferToImage(GpuBuffer buffer, Texture image, int mip, int layer);
    void copyImageToImage(Texture src, Texture dst, int srcMip, int srcLayer, int dstMip, int dstLayer);

}
