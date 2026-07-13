package com.vke.core.vulkan.command;

import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.data.GpuBuffer;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.pipeline.RenderPipeline;
import com.vke.api.rendering.abstraction.pipeline.Pipeline;
import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.vulkan.ImageLayout;
import com.vke.api.rendering.vulkan.ImageState;
import com.vke.api.rendering.vulkan.descriptors.bindings.BufferBinding;
import com.vke.api.rendering.vulkan.pipeline.IVulkanPipeline;
import com.vke.core.vulkan.Scissor;
import com.vke.core.vulkan.Viewport;
import com.vke.core.vulkan.buffers.MappedGpuRingBuffer;
import com.vke.core.vulkan.buffers.VulkanGpuBuffer;
import com.vke.core.vulkan.descriptor.EngineDescriptorSetsManager;
import com.vke.core.vulkan.descriptor.ds2.DescriptorSetInstance;
import com.vke.core.vulkan.pipeline.VulkanPipelineLayout;
import com.vke.core.vulkan.service.VulkanRenderSystem;
import com.vke.core.vulkan.swapchain.VulkanSwapchain;
import com.vke.core.vulkan.texture.texture2.VulkanTexture;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class VulkanCmdBuffers implements CommandBuffer {

    private final long poolHandle;
    private final VkCommandBuffer vk;
    private final EngineDescriptorSetsManager setsMgr;
    private final VulkanRenderSystem ctx;
    private final VulkanSwapchain swapchain;

    private boolean recording;

    public VulkanCmdBuffers(VulkanRenderSystem ctx, CommandPool pool, FrameCounter fc) {
        this.poolHandle = pool.getHandle();
        this.setsMgr = ctx.renderer().getEngineSetsManager();
        this.ctx = ctx;
        this.swapchain = ctx.swapchain();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack)
                    .sType$Default()
                    .commandPool(poolHandle)
                    .level(VK14.VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                    .commandBufferCount(1);

            PointerBuffer pCommandBuffer = stack.mallocPointer(1);

            if (VK14.vkAllocateCommandBuffers(ctx.device().vkLogicalDevice(), allocInfo, pCommandBuffer) != VK14.VK_SUCCESS) {
                throw new IllegalStateException("Failed to create command buffer!");
            }

            this.vk = new VkCommandBuffer(pCommandBuffer.get(0), ctx.device().vkLogicalDevice());
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
            currentColorImage.transition(this, ImageState.COLOR_ATTACHMENT);

            VulkanTexture currentDepthImage = swapchain.getDepthImage(swapchain.currentImageIndex());
            currentDepthImage.transition(this, ImageState.DEPTH_ATTACHMENT);

            VkClearValue clearColor = VkClearValue.calloc(stack).color(VkClearColorValue.calloc(stack)
                    .float32(0, 0.2f).float32(1, 0.3f).float32(2, 0.3f).float32(3, 1.0f));

            VkRenderingAttachmentInfo.Buffer buffer = VkRenderingAttachmentInfo.calloc(1, stack);
            buffer.get(0)
                    .sType$Default()
                    .imageView(currentColorImage.defaultView().getHandle())
                    .imageLayout(VK14.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
                    .loadOp(VK14.VK_ATTACHMENT_LOAD_OP_CLEAR)
                    .storeOp(VK14.VK_ATTACHMENT_STORE_OP_STORE)
                    .clearValue(clearColor);

            VkRenderingAttachmentInfo depth = VkRenderingAttachmentInfo.calloc(stack)
                    .sType$Default()
                    .imageView(currentDepthImage.defaultView().getHandle())
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
        currentImage.transition(this, ImageState.PRESENT);

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
            return (IVulkanPipeline) pipeline.acquire(ctx);
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
        if (layout.pushConstants() == null) return;

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
        List<Integer> dynamicOffsets = l.getSets().stream()
                .flatMap(instance -> instance.bindings.values().stream()
                        .filter(binding -> binding instanceof BufferBinding)
                        .map(binding -> ((BufferBinding) binding).buffer)
                        .filter(buf -> buf instanceof MappedGpuRingBuffer)
                        .map(buf -> (int) ((MappedGpuRingBuffer) buf).getOffset())
                        .sorted(Comparator.comparingInt(c -> c))
                ).collect(Collectors.toCollection(ArrayList::new));;

        List<DescriptorSetInstance> userSets = l.getSets();
        for (int i = 0; i < userSets.size(); i++) {
            DescriptorSetInstance userSet = userSets.get(i);
            sets[i] = userSet.getSet().handle();
        }

        //l.getGroup().getHandleCache().values().stream()
        //        .filter(h -> h instanceof BufferHandle b && b.bufBinding.layout.type.isDynamic())
        //        .sorted(Comparator.comparingInt((UniformHandle h) -> h.set).thenComparingInt(h -> h.binding))
        //        .forEach(h -> dynamicOffsets.add((int) ((BufferHandle) h).getOffset()));



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

    public void bindIndexBuffer(VulkanGpuBuffer buffer, int offset) {
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

    @Override
    public void copyBufferToImage(GpuBuffer buffer, Texture image, int mip, int layer) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferImageCopy.Buffer region = VkBufferImageCopy.calloc(1, stack);

            region.get(0)
                    .bufferOffset(0)
                    .bufferRowLength(0)
                    .bufferImageHeight(0)
                    .imageSubresource()
                    .aspectMask(((VulkanTexture) image).getAspect().getVkHandle())
                    .mipLevel(mip)
                    .baseArrayLayer(layer)
                    .layerCount(1);

            region.imageOffset().set(0, 0, 0);
            region.imageExtent().set(
                    image.width(mip),
                    image.height(mip),
                    1
            );

            VK14.vkCmdCopyBufferToImage(
                    this.getBuffer(),
                    ((VulkanGpuBuffer) buffer).getBuffer(),
                    ((VulkanTexture) image).getHandle(),
                    VK14.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    region
            );
        }
    }

    public VkCommandBuffer getBuffer() { return this.vk; }

    @Override
    public void free() {
        VK14.vkFreeCommandBuffers(ctx.device().vkLogicalDevice(), poolHandle, vk);
    }
}
