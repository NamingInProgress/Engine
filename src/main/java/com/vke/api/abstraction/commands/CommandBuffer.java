package com.vke.api.abstraction.commands;

import com.vke.api.abstraction.pipeline.ComputePipeline;
import com.vke.api.abstraction.pipeline.GraphicsPipeline;
import com.vke.core.rendering.vulkan.Scissor;
import com.vke.core.rendering.vulkan.Viewport;
import com.vke.utils.Disposable;

public interface CommandBuffer extends Disposable {

    record SubmitInfo() {}

    boolean isRecording();

    void begin();
    void end();
    void reset();

    void bindRenderPipeline(GraphicsPipeline pipeline);
    void bindComputePipeline(ComputePipeline pipeline);

    void setPushConstants(GraphicsPipeline pipeline);
    void setDescriptorSets(GraphicsPipeline pipeline);

    void setViewport(Viewport viewport);
    void setScissor(Scissor scissor);

    void draw(int vertexCount, int instanceCount, int firstVertex, int firstInstance);
    void drawIndexed(int indexCount, int instanceCount, int firstIndex, int vertexOffset, int firstInstance);

}
