package com.vke.core.rendering.vulkan.command;

import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.GpuBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.LoadOp;
import com.vke.api.rendering.abstraction.renderer.enums.StoreOp;
import com.vke.api.rendering.abstraction.renderer.pipeline.RenderPipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.Pipeline;
import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.vulkan.ImageLayout;
import com.vke.api.rendering.vulkan.ImageState;
import com.vke.api.rendering.vulkan.descriptors.bindings.BufferBinding;
import com.vke.api.rendering.vulkan.pipeline.IVulkanPipeline;
import com.vke.core.geometry.Rect;
import com.vke.core.rendering.vulkan.Scissor;
import com.vke.core.rendering.vulkan.Viewport;
import com.vke.core.rendering.vulkan.buffers.MappedGpuRingBuffer;
import com.vke.core.rendering.vulkan.buffers.VulkanGpuBuffer;
import com.vke.core.rendering.vulkan.descriptor.EngineDescriptorSetsManager;
import com.vke.core.rendering.vulkan.descriptor.ds2.DescriptorSetInstance;
import com.vke.core.rendering.vulkan.pipeline.VulkanPipelineLayout;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import com.vke.core.rendering.vulkan.swapchain.VulkanSwapchain;
import com.vke.core.rendering.vulkan.texture.VulkanImageView;
import com.vke.core.rendering.vulkan.texture.VulkanTexture;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VulkanCmdBuffers implements CommandBuffer {

    private final long poolHandle;
    private final VkCommandBuffer vk;
    private final EngineDescriptorSetsManager setsMgr;
    private final VulkanRenderSystem ctx;
    private final VulkanSwapchain swapchain;

    private boolean recording, rendering;

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
        VulkanTexture currentColorImage = swapchain.getColorImage(swapchain.currentImageIndex());

        this.beginRendering(new RenderingInfo(
                List.of(new AttachmentInfo(currentColorImage, LoadOp.CLEAR, StoreOp.STORE, new float[]{ 0.2f, 0.3f, 0.3f, 1.0f })),
                null
        ));
    }

    @Override
    public void beginRendering(RenderingInfo info) {
        if (rendering) throw new IllegalStateException("Tried to begin rendering while rendering!");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            this.rendering = true;

            VkRenderingAttachmentInfo.Buffer buffer = null;

            if (info.colorAttachments() != null && !info.colorAttachments().isEmpty()) {
                buffer = VkRenderingAttachmentInfo.calloc(info.colorAttachments().size(), stack);

                List<AttachmentInfo> colorAttachments = info.colorAttachments();
                for (int i = 0; i < colorAttachments.size(); i++) {
                    AttachmentInfo colorAttachment = colorAttachments.get(i);
                    VulkanTexture tex = (VulkanTexture) colorAttachment.tex();
                    tex.transition(this, ImageState.COLOR_ATTACHMENT);
                    VkClearValue clearColor = VkClearValue.calloc(stack).color(VkClearColorValue.calloc(stack)
                            .float32(0, colorAttachment.clearColor()[0])
                            .float32(1, colorAttachment.clearColor()[1])
                            .float32(2, colorAttachment.clearColor()[2])
                            .float32(3, colorAttachment.clearColor()[3]));

                    buffer.get(i)
                            .sType$Default()
                            .imageView(((VulkanImageView) colorAttachment.view()).getHandle())
                            .imageLayout(VK14.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
                            .loadOp(colorAttachment.loadOp().getVkHandle())
                            .storeOp(colorAttachment.storeOp().getVkHandle())
                            .clearValue(clearColor);
                }
            }

            VkRenderingAttachmentInfo depth = null;
            VkRenderingAttachmentInfo stencil = null;
            if (info.depthAttachment() != null) {
                ((VulkanTexture) info.depthAttachment().tex()).transition(this, ImageState.DEPTH_STENCIL_ATTACHMENT);
                depth = VkRenderingAttachmentInfo.calloc(stack)
                        .sType$Default()
                        .imageView(((VulkanImageView) info.depthAttachment().view()).getHandle())
                        .imageLayout(ImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL.getVkHandle())
                        .loadOp(info.depthAttachment().loadOp().getVkHandle())
                        .storeOp(info.depthAttachment().storeOp().getVkHandle())
                        .clearValue((v) -> v.depthStencil().depth(info.depthAttachment().clearColor()[0]));
            }

            if (info.stencilAttachment() != null) {
                ((VulkanTexture) info.stencilAttachment().tex()).transition(this, ImageState.DEPTH_STENCIL_ATTACHMENT);
                stencil = VkRenderingAttachmentInfo.calloc(stack)
                        .sType$Default()
                        .imageView(((VulkanImageView) info.stencilAttachment().view()).getHandle())
                        .imageLayout(ImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL.getVkHandle())
                        .loadOp(info.stencilAttachment().loadOp().getVkHandle())
                        .storeOp(info.stencilAttachment().storeOp().getVkHandle())
                        .clearValue((v) -> v.depthStencil().depth(info.stencilAttachment().clearColor()[0]));
            }


            VkRect2D area = VkRect2D.calloc(stack);
            Rect renderArea = info.renderArea();
            if (renderArea != null) {
                area.offset(offset -> offset.set(renderArea.x, renderArea.y));
                area.extent(extent -> extent.set(renderArea.width, renderArea.height));
            } else {
                area.offset(offset -> offset.set(0, 0));
                area.extent(swapchain.getExtent());
            }

            VkRenderingInfo renderInfo = VkRenderingInfo.calloc(stack)
                    .sType$Default()
                    .layerCount(1)
                    .renderArea(area)
                    .pColorAttachments(buffer);

            if (info.depthAttachment() != null) renderInfo.pDepthAttachment(depth);
            if (info.stencilAttachment() != null) renderInfo.pStencilAttachment(stencil);

            VK14.vkCmdBeginRendering(vk, renderInfo);
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

        if (rendering) throw new IllegalStateException("Tried to end while rendering!");

        VulkanTexture currentImage = swapchain.getColorImage(swapchain.currentImageIndex());
        currentImage.transition(this, ImageState.PRESENT);

        VK14.vkEndCommandBuffer(vk);
    }

    @Override
    public void endRendering() {
        this.rendering = false;
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
                .flatMap(instance -> instance.bindings.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(Map.Entry::getValue)
                        .filter(binding -> binding instanceof BufferBinding)
                        .map(binding -> ((BufferBinding) binding).buffer)
                        .filter(buf -> buf instanceof MappedGpuRingBuffer)
                        .map(buf -> (int) ((MappedGpuRingBuffer) buf).getOffset()))
                .collect(Collectors.toCollection(ArrayList::new));

        List<DescriptorSetInstance> userSets = l.getSets();
        for (int i = 0; i < userSets.size(); i++) {
            DescriptorSetInstance userSet = userSets.get(i);
            sets[i] = userSet.getSet(true).handle();
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

    @Override
    public void copyImageToImage(Texture src, Texture dst, int srcMip, int srcLayer, int dstMip, int dstLayer) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VulkanTexture vkSrc = (VulkanTexture) src;
            VulkanTexture vkDst = (VulkanTexture) dst;
            vkSrc.transition(this, ImageState.TRANSFER_SRC);
            vkDst.transition(this, ImageState.TRANSFER_DST);
            VkImageBlit2.Buffer region = VkImageBlit2.calloc(1, stack);

            region.get(0)
                    .sType$Default()
                    .srcSubresource(s -> s
                            .aspectMask(vkSrc.getAspect().getVkHandle())
                            .mipLevel(srcMip)
                            .baseArrayLayer(srcLayer)
                            .layerCount(1))
                    .dstSubresource(s -> s
                            .aspectMask(vkDst.getAspect().getVkHandle())
                            .mipLevel(dstMip)
                            .baseArrayLayer(dstLayer)
                            .layerCount(1));

            region.get(0).srcOffsets(0)
                    .set(0, 0, 0);
            region.get(0).srcOffsets(1)
                    .set(src.width(), src.height(), 1);
            region.get(0).dstOffsets(0)
                    .set(0, 0, 0);
            region.get(0).dstOffsets(1)
                    .set(dst.width(), dst.height(), 1);

            VkBlitImageInfo2 info = VkBlitImageInfo2.calloc(stack)
                    .sType$Default()
                    .srcImage(vkSrc.getHandle())
                    .srcImageLayout(vkSrc.getState().getLayoutHandle())
                    .dstImage(vkDst.getHandle())
                    .dstImageLayout(vkDst.getState().getLayoutHandle())
                    .filter(VK14.VK_FILTER_LINEAR)
                    .pRegions(region);

            VK14.vkCmdBlitImage2(this.getBuffer(), info);
        }
    }

    public VkCommandBuffer getBuffer() { return this.vk; }

    @Override
    public void free() {
        VK14.vkFreeCommandBuffers(ctx.device().vkLogicalDevice(), poolHandle, vk);
    }
}
