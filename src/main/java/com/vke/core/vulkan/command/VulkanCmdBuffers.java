package com.vke.core.vulkan.command;

import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.pipeline.RenderPipeline;
import com.vke.api.rendering.abstraction.pipeline.Pipeline;
import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.vulkan.ImageLayout;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.BufferHandle;
import com.vke.api.rendering.vulkan.pipeline.IVulkanPipeline;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.Scissor;
import com.vke.core.vulkan.Viewport;
import com.vke.core.vulkan.buffers.GpuBuffer;
import com.vke.core.vulkan.descriptor.EngineDescriptorSetsManager;
import com.vke.core.vulkan.descriptor.ds2.DescriptorSetInstance;
import com.vke.core.vulkan.device.LogicalDevice;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.pipeline.VulkanPipelineLayout;
import com.vke.core.vulkan.swapchain.VulkanSwapchain;
import com.vke.core.vulkan.texture.VulkanTexture;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class VulkanCmdBuffers implements CommandBuffer {

    private final long poolHandle;
    private final LogicalDevice device;
    private final VkCommandBuffer vk;
    private final VulkanSwapchain swapchain;
    private final VKEngine engine;
    private final FrameCounter fc;
    private final int highestEngineSet;
    private final EngineDescriptorSetsManager setsMgr;

    private boolean recording;
    private boolean engineDescriptorSetsBound = false;

    public VulkanCmdBuffers(VKEngine engine, VulkanRenderDevice device, VulkanSwapchain swapchain, CommandPool pool, FrameCounter fc) {
        this.device = device.getLogicalDevice();
        this.poolHandle = pool.getHandle();
        this.swapchain = swapchain;
        this.engine = engine;
        this.fc = fc;
        this.setsMgr = device.getRenderer().getEngineSetsManager();
        this.highestEngineSet = setsMgr.highestSet;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack)
                    .sType$Default()
                    .commandPool(poolHandle)
                    .level(VK14.VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                    .commandBufferCount(1);

            PointerBuffer pCommandBuffer = stack.mallocPointer(1);

            if (VK14.vkAllocateCommandBuffers(this.device.getDevice(), allocInfo, pCommandBuffer) != VK14.VK_SUCCESS) {
                throw new IllegalStateException("Failed to create command buffer!");
            }

            this.vk = new VkCommandBuffer(pCommandBuffer.get(0), this.device.getDevice());
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
        }
    }

    @Override
    public void beginRendering() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VulkanTexture currentColorImage = swapchain.getColorImage(swapchain.currentImageIndex());
            currentColorImage.getImage().transitionLayout(
                    this,
                    ImageLayout.COLOR_ATTACHMENT_OPTIMAL,
                    0,
                    VK14.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT,
                    VK14.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT,
                    VK14.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT
            );

            VulkanTexture currentDepthImage = swapchain.getDepthImage(swapchain.currentImageIndex());
            currentDepthImage.getImage().transitionLayout(
                    this,
                    ImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
                    0,
                    VK14.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK14.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT,
                    VK14.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT,
                    VK14.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT
            );

            VkClearValue clearColor = VkClearValue.calloc(stack).color(VkClearColorValue.calloc(stack)
                    .float32(0, 0.2f).float32(1, 0.3f).float32(2, 0.3f).float32(3, 1.0f));

            VkRenderingAttachmentInfo.Buffer buffer = VkRenderingAttachmentInfo.calloc(1, stack);
            buffer.get(0)
                    .sType$Default()
                    .imageView(currentColorImage.getView().getHandle())
                    .imageLayout(VK14.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
                    .loadOp(VK14.VK_ATTACHMENT_LOAD_OP_CLEAR)
                    .storeOp(VK14.VK_ATTACHMENT_STORE_OP_STORE)
                    .clearValue(clearColor);

            VkRenderingAttachmentInfo depth = VkRenderingAttachmentInfo.calloc(stack)
                    .sType$Default()
                    .imageView(currentDepthImage.getView().getHandle())
                    .imageLayout(ImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL.getVkHandle())
                    .loadOp(VK14.VK_ATTACHMENT_LOAD_OP_CLEAR)
                    .storeOp(VK14.VK_ATTACHMENT_STORE_OP_STORE)
                    .clearValue((v) -> v.depthStencil().depth(1.0f));


            VkRect2D area = VkRect2D.calloc(stack);
            area.extent(swapchain.getExtent());

            // add color attachments, depth attachments and stencil attachments store image on swapchain and yeah
            VkRenderingInfo info = VkRenderingInfo.calloc(stack)
                    .sType$Default()
                    .layerCount(1)
                    .renderArea(area)
                    .pColorAttachments(buffer)
                    .pDepthAttachment(depth);

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

        VulkanTexture currentImage = swapchain.getColorImage(swapchain.currentImageIndex());
        currentImage.getImage().transitionLayout(
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
    public void endRendering() {
        VK14.vkCmdEndRendering(vk);
    }

    @Override
    public void reset() {
        VK14.vkResetCommandBuffer(vk, 0);
    }

    private IVulkanPipeline unwrapPipeline(AssetHandle<? extends Pipeline> pipeline) {
        try {
            return (IVulkanPipeline) pipeline.acquire(engine);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int getBindPoint(IVulkanPipeline pipeline) {
        return pipeline instanceof RenderPipeline ? VK14.VK_PIPELINE_BIND_POINT_GRAPHICS : VK14.VK_PIPELINE_BIND_POINT_COMPUTE;
    }

    @Override
    public void bindPipeline(AssetHandle<? extends Pipeline> pipeline) {
        IVulkanPipeline vkPipeline = unwrapPipeline(pipeline);
        VK14.vkCmdBindPipeline(this.getBuffer(),
                getBindPoint(vkPipeline), vkPipeline.getHandle());
    }

    @Override
    public void setPushConstants(AssetHandle<? extends Pipeline> pipeline) {
        VulkanPipelineLayout layout = (VulkanPipelineLayout) unwrapPipeline(pipeline).layout();

        VK14.vkCmdPushConstants(this.getBuffer(),
                layout.getHandle(),
                VK14.VK_SHADER_STAGE_ALL,
                0,
                layout.pushConstants().getData()
        );
    }

    @Override
    public void bindDescriptorSets(AssetHandle<? extends Pipeline> pipeline) {
        IVulkanPipeline p = unwrapPipeline(pipeline);
        VulkanPipelineLayout l = (VulkanPipelineLayout) p.layout();
        l.writeHandles();

        long[] sets = new long[l.getSets().size()];
        List<Integer> dynamicOffsets = setsMgr.getDynamicOffsets();

        List<DescriptorSetInstance> userSets = l.getSets();
        for (int i = 0; i < userSets.size(); i++) {
            DescriptorSetInstance userSet = userSets.get(i);
            sets[i] = userSet.getSet().getHandle();
        }

        l.getGroup().getHandleCache().values().stream()
                .filter(h -> h instanceof BufferHandle b && b.bufBinding.layout.type.isDynamic())
                .sorted(Comparator.comparingInt((UniformHandle h) -> h.set).thenComparingInt(h -> h.binding))
                .forEach(h -> dynamicOffsets.add((int) ((BufferHandle) h).getOffset()));

        VK14.vkCmdBindDescriptorSets(this.getBuffer(),
                getBindPoint(p),
                l.getHandle(),
                0, sets,
                dynamicOffsets.stream()
                        .mapToInt(Integer::intValue)
                        .toArray());
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

    public void bindIndexBuffer(GpuBuffer buffer, int offset) {
        VK14.vkCmdBindIndexBuffer(this.getBuffer(), buffer.getBuffer(), offset, VK14.VK_INDEX_TYPE_UINT32);
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
