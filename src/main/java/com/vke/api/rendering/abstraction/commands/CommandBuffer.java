package com.vke.api.rendering.abstraction.commands;

import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.abstraction.pipeline.ComputePipeline;
import com.vke.api.rendering.abstraction.pipeline.RenderPipeline;
import com.vke.api.rendering.abstraction.pipeline.Pipeline;
import com.vke.api.rendering.abstraction.sync.Fence;
import com.vke.api.rendering.abstraction.sync.Semaphore;
import com.vke.core.vulkan.Scissor;
import com.vke.core.vulkan.Viewport;
import com.vke.api.rendering.abstraction.enums.QueueType;
import com.vke.utils.io.Disposable;

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

    boolean isRecording();

    void begin();
    void beginRendering();
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

}
