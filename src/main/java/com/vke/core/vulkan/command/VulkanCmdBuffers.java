package com.vke.core.vulkan.command;

import com.vke.api.abstraction.commands.CommandBuffer;
import com.vke.api.abstraction.pipeline.ComputePipeline;
import com.vke.api.abstraction.pipeline.GraphicsPipeline;
import com.vke.api.utils.AlignedByteBuffer;
import com.vke.api.vulkan.ImageLayout;
import com.vke.api.vulkan.pipeline.PushConstantsDefinition;
import com.vke.core.rendering.vulkan.Scissor;
import com.vke.core.rendering.vulkan.Viewport;
import com.vke.core.rendering.vulkan.commands.CommandPool;
import com.vke.core.rendering.vulkan.descriptor.DescriptorSet;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.vulkan.pipeline.PipelineLayout;
import com.vke.core.vulkan.swapchain.SwapchainImageView;
import com.vke.core.vulkan.swapchain.VulkanSwapchain;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

public class VulkanCmdBuffers implements CommandBuffer {

    private final long poolHandle;
    private final LogicalDevice device;
    private final VkCommandBuffer vk;
    private final VulkanSwapchain swapchain;

    private boolean recording;

    public VulkanCmdBuffers(LogicalDevice device, VulkanSwapchain swapchain, CommandPool pool) {
        this.device = device;
        this.poolHandle = pool.getHandle();
        this.swapchain = swapchain;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack)
                    .sType$Default()
                    .commandPool(poolHandle)
                    .level(VK14.VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                    .commandBufferCount(1);

            PointerBuffer pCommandBuffer = stack.mallocPointer(1);

            if (VK14.vkAllocateCommandBuffers(device.getDevice(), allocInfo, pCommandBuffer) != VK14.VK_SUCCESS) {
                throw new IllegalStateException("Failed to create command buffer!");
            }

            this.vk = new VkCommandBuffer(pCommandBuffer.get(0), device.getDevice());
        }
    }

    @Override
    public boolean isRecording() {
        return recording;
    }

    @Override
    public void begin() {
        this.recording = true;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack)
                    .sType$Default().flags(VK14.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            VK14.vkBeginCommandBuffer(this.vk, beginInfo);

            SwapchainImageView currentImage = swapchain.getImageView(swapchain.currentImageIndex());
            currentImage.transitionLayout(
                    this,
                    ImageLayout.COLOR_ATTACHMENT_OPTIMAL,
                    0,
                    VK14.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT,
                    VK14.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT,
                    VK14.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT
            );

            VkClearValue clearColor = VkClearValue.calloc(stack).color(VkClearColorValue.calloc(stack)
                    .float32(0, 0.2f).float32(1, 0.3f).float32(2, 0.3f).float32(3, 1.0f));

            VkRenderingAttachmentInfo.Buffer buffer = VkRenderingAttachmentInfo.calloc(1, stack);
            buffer.get(0)
                    .sType$Default()
                    .imageView(currentImage.getHandle())
                    .imageLayout(VK14.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
                    .loadOp(VK14.VK_ATTACHMENT_LOAD_OP_CLEAR)
                    .storeOp(VK14.VK_ATTACHMENT_STORE_OP_STORE)
                    .clearValue(clearColor);


            VkRect2D area = VkRect2D.calloc(stack);
            area.extent(swapchain.getExtent());

            VkRenderingInfo info = VkRenderingInfo.calloc(stack)
                    .sType$Default()
                    .layerCount(1)
                    .renderArea(area)
                    .pColorAttachments(buffer);

            VK14.vkCmdBeginRendering(vk, info);
        }
    }

    public void beginImmediate() {
        this.recording = true;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack)
                    .sType$Default().flags(VK14.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            VK14.vkBeginCommandBuffer(this.vk, beginInfo);
        }
    }

    public void endImmediate() {
        this.recording = false;
        VK14.vkEndCommandBuffer(vk);
    }

    @Override
    public void end() {
        this.recording = false;
        VK14.vkCmdEndRendering(vk);

        SwapchainImageView currentImage = swapchain.getImageView(swapchain.currentImageIndex());
        currentImage.transitionLayout(
                this,
                ImageLayout.PRESENT_SRC_KHR,
                VK14.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT,
                0,
                VK14.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT,
                VK14.VK_PIPELINE_STAGE_2_BOTTOM_OF_PIPE_BIT
        );

        VK14.vkEndCommandBuffer(vk);
    }

    @Override
    public void reset() {
        VK14.vkResetCommandBuffer(vk, 0);
    }

    @Override
    public void bindRenderPipeline(GraphicsPipeline pipeline) {
        VK14.vkCmdBindPipeline(this.getBuffer(),
                VK14.VK_PIPELINE_BIND_POINT_GRAPHICS, ((com.vke.core.vulkan.pipeline.GraphicsPipeline) pipeline).getHandle());
    }

    @Override
    public void bindComputePipeline(ComputePipeline pipeline) {
        throw new RuntimeException("Compute Pipelines are not implemented yet!");
        //VK14.vkCmdBindPipeline(this.getBuffer(),
        //        VK14.VK_PIPELINE_BIND_POINT_COMPUTE, pipeline.getHandle());
    }

    @Override
    public void setPushConstants(GraphicsPipeline pipeline) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PipelineLayout layout = (PipelineLayout) pipeline.layout();
            layout.getPushConstants().forEach((k, v) -> {
                int size = v.getSize(PushConstantsDefinition.ALIGN);
                AlignedByteBuffer buf = new AlignedByteBuffer(stack.calloc(size), PushConstantsDefinition.ALIGN);
                VK14.vkCmdPushConstants(this.getBuffer(),
                        layout.getHandle(),
                        v.getAplicableStages().getVkHandle(),
                        v.getOffset(),
                        v.getBytes(buf));
            });
        }
    }

    @Override
    public void setDescriptorSets(GraphicsPipeline pipeline) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer sets = stack.longs(
                    ((com.vke.core.vulkan.pipeline.GraphicsPipeline) pipeline)
                            .getDescriptorSets()
                            .stream()
                            .mapToLong(DescriptorSet::getHandle)
                            .toArray());

            VK14.vkCmdBindDescriptorSets(this.getBuffer(),
                    VK14.VK_PIPELINE_BIND_POINT_GRAPHICS,
                    ((PipelineLayout) pipeline.layout()).getHandle(),
                    0, sets, null);
        }
    }

    @Override
    public void setViewport(Viewport viewport) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkViewport.Buffer viewportBuffer = VkViewport.calloc(1, stack);
            viewportBuffer.get(0)
                    .set(viewport.x, viewport.h,
                            viewport.w, -viewport.h,
                            viewport.minDepth, viewport.maxDepth);

            VK14.vkCmdSetViewport(this.getBuffer(), 0, viewportBuffer);
        }
    }

    @Override
    public void setScissor(Scissor scissor) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkRect2D.Buffer scissorBuffer = VkRect2D.calloc(1, stack);
            scissorBuffer.get(0)
                    .set(VkRect2D.calloc(stack)
                            .offset(VkOffset2D.calloc(stack).set(scissor.x, scissor.y))
                            .extent(VkExtent2D.calloc(stack).set(scissor.w, scissor.h))
                    );

            VK14.vkCmdSetScissor(this.getBuffer(), 0, scissorBuffer);
        }
    }

    @Override
    public void draw(int vertexCount, int instanceCount, int firstVertex, int firstInstance) {
        VK14.vkCmdDraw(this.getBuffer(), vertexCount, instanceCount, firstVertex, firstInstance);
    }

    @Override
    public void drawIndexed(int indexCount, int instanceCount, int firstIndex, int vertexOffset, int firstInstance) {
        VK14.vkCmdDrawIndexed(this.getBuffer(), indexCount, instanceCount, firstIndex, vertexOffset, firstInstance);
    }

    public VkCommandBuffer getBuffer() { return this.vk; }

    @Override
    public void free() {
        VK14.vkFreeCommandBuffers(device.getDevice(), poolHandle, vk);
    }
}
