package com.vke.core.rendering.vulkan.commands;

import com.vke.core.VKEngine;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.rendering.vulkan.pipeline.GraphicsPipeline;
import com.vke.core.rendering.vulkan.swapchain.ImageView;
import com.vke.core.rendering.vulkan.swapchain.SwapChain;
import com.vke.utils.Disposable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

public class CommandBuffers implements Disposable {
    private static final String HERE = "Command Buffers";

    private final long poolHandle;
    private final VkCommandBuffer vk;
    private final LogicalDevice device;
    private AutoHeapAllocator alloc;
    private VkCommandBufferBeginInfo beginInfo;

    public CommandBuffers(VKEngine engine, CommandPool pool, LogicalDevice device, int count) {
        this.device = device;
        this.poolHandle = pool.getHandle();
        this.alloc = new AutoHeapAllocator();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack)
                    .sType$Default()
                    .commandPool(poolHandle)
                    .level(VK14.VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                    .commandBufferCount(count);

            PointerBuffer pCommandBuffer = stack.mallocPointer(1);

            if (VK14.vkAllocateCommandBuffers(device.getDevice(), allocInfo, pCommandBuffer) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create command buffer!"), HERE);
            }

            this.vk = new VkCommandBuffer(pCommandBuffer.get(0), device.getDevice());
        }

        this.beginInfo = alloc.allocStruct(VkCommandBufferBeginInfo.SIZEOF, VkCommandBufferBeginInfo::new)
                .sType$Default()
                .flags(VK14.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
    }

    public VkCommandBuffer getBuffer() { return this.vk; }

    public void startRecording(MemoryStack stack, SwapChain swapChain) {
        VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack)
                        .sType$Default().flags(VK14.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
        VK14.vkBeginCommandBuffer(this.vk, beginInfo);

        ImageView currentImage = swapChain.getImageViews().get(swapChain.currentImageIndex());
        currentImage.transitionLayout(
                this,
                VK14.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
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
        area.extent(swapChain.getExtent());

        VkRenderingInfo info = VkRenderingInfo.calloc(stack)
                .sType$Default()
                .layerCount(1)
                .renderArea(area)
                .pColorAttachments(buffer);

        VK14.vkCmdBeginRendering(vk, info);
    }

    public void endRecording(SwapChain swapChain) {
        VK14.vkCmdEndRendering(vk);

        ImageView currentImage = swapChain.getImageViews().get(swapChain.currentImageIndex());
        currentImage.transitionLayout(
                this,
                KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR,
                VK14.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT,
                0,
                VK14.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT,
                VK14.VK_PIPELINE_STAGE_2_BOTTOM_OF_PIPE_BIT
        );

        VK14.vkEndCommandBuffer(vk);
    }

    public void reset() {
        VK14.vkResetCommandBuffer(vk, 0);
    }

    public void bindPipeline(GraphicsPipeline pipeline, int type) {
        VK14.vkCmdBindPipeline(this.getBuffer(), type, pipeline.getHandle());
    }

    public void setViewport(int firstViewport, VkViewport.Buffer buffer) {
        VK14.vkCmdSetViewport(this.getBuffer(), firstViewport, buffer);
    }

    public void setScissor(int firstScissor, VkRect2D.Buffer buffer) {
        VK14.vkCmdSetScissor(this.getBuffer(), firstScissor, buffer);
    }

    @Override
    public void free() {
        VK14.vkFreeCommandBuffers(device.getDevice(), poolHandle, vk);
        alloc.close();
    }

    private static final AutoHeapAllocator infoAlloc = new AutoHeapAllocator();
    private static VkCommandBufferSubmitInfo submitInfo;

    public static VkCommandBufferSubmitInfo getDefaultSubmitInfo(CommandBuffers buffers) {
        //if (submitInfo == null) {
            submitInfo = infoAlloc.allocStruct(VkCommandBufferSubmitInfo.SIZEOF, VkCommandBufferSubmitInfo::new);
            submitInfo.sType$Default();
            submitInfo.deviceMask(0);
        //}
        submitInfo.commandBuffer(buffers.getBuffer());
        return submitInfo;
    }

    public static void freeSubmitInfo() {
        submitInfo.close();
    }

}
