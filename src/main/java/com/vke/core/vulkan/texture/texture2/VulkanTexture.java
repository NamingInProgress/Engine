package com.vke.core.vulkan.texture.texture2;

import com.vke.api.rendering.abstraction.data.ImageView;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.enums.buffer.BufferUsage;
import com.vke.api.rendering.abstraction.enums.buffer.MemoryUsage;
import com.vke.api.rendering.abstraction.enums.texture.ImageAspect;
import com.vke.api.rendering.vulkan.ImageLayout;
import com.vke.api.rendering.vulkan.ImageState;
import com.vke.api.rendering.vulkan.buffer.CpuBuffer;
import com.vke.api.rendering.vulkan.memory.VulkanImageBarrier;
import com.vke.core.file.png.Pixels;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.rendering.draw.FrameContext;
import com.vke.core.vulkan.buffers.StagedBuffer;
import com.vke.core.vulkan.buffers.VulkanGpuBuffer;
import com.vke.core.vulkan.buffers.premade.GeneralBuffer;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.extent.VulkanExtentUtils;
import com.vke.utils.io.Disposable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.*;

import java.awt.*;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class VulkanTexture implements Texture {

    private final TextureDesc desc;
    private final VulkanRenderDevice device;

    private final long handle;
    private final long allocation;

    private final HashMap<ImageView.ImageViewDesc, ImageView> views = new HashMap<>();

    private VulkanImageView defaultView;
    private ImageState state = ImageState.UNDEFINED; // [mipLevel][arrayLayer]
    private ImageAspect aspect = ImageAspect.AUTO;

    public VulkanTexture(VulkanRenderDevice device, long handle, TextureDesc desc, ImageAspect aspect) {
        this.desc = desc;
        this.device = device;
        this.handle = handle;
        this.allocation = 0;
    }

    public VulkanTexture(VulkanRenderDevice device, TextureDesc desc) {
        this.desc = desc;
        this.device = device;
        this.aspect = this.aspect.resolve(desc.format);

//        for (int mip = 0; mip < desc.mipLevels; mip++) {
//            for (int layer = 0; layer < desc.arrayLayers; layer++) {
//                state[mip][layer] = ImageState.UNDEFINED;
//            }
//        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkImageCreateInfo imageCreateInfo = VkImageCreateInfo.calloc(stack)
                    .sType$Default()
                    .imageType(desc.type.getVkHandle())
                    .format(desc.format.getVkHandle())
                    .extent(VulkanExtentUtils.createVk3D(stack, desc.extent))
                    .mipLevels(desc.mipLevels)
                    .arrayLayers(desc.arrayLayers)
                    .samples(desc.samples.getVkHandle())
                    .tiling(desc.tiling.getVkHandle())
                    .usage(desc.usage.getVkHandle());

            if (desc.cubeMap)
                imageCreateInfo.flags(VK14.VK_IMAGE_CREATE_CUBE_COMPATIBLE_BIT);

            VmaAllocationCreateInfo allocInfo = VmaAllocationCreateInfo.calloc(stack)
                    .usage(desc.memUsage.getVkHandle())
                    .requiredFlags(VK14.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);

            LongBuffer pImage = stack.mallocLong(1);
            PointerBuffer pAllocation = stack.mallocPointer(1);
            Vma.vmaCreateImage(device.getVmaAllocator(), imageCreateInfo, allocInfo, pImage, pAllocation, null);
            this.handle = pImage.get(0);
            this.allocation = pAllocation.get(0);
        }
    }

    @Override
    public VulkanImageView defaultView() {
        if (defaultView != null) return defaultView;
        var description = new ImageView.ImageViewDesc(this, desc.type, format(), 0,
                desc.mipLevels, 0, desc.arrayLayers, ImageAspect.AUTO);
        this.defaultView = new VulkanImageView(device, description);
        this.views.put(description, defaultView);
        return defaultView;
    }

    @Override
    public ImageView getView(Consumer<ImageView.ImageViewDescriptionBuilder> consumer) {
        var builder = new ImageView.ImageViewDescriptionBuilder(this);
        consumer.accept(builder);
        var desc = builder.build();

        if (views.containsKey(desc)) return views.get(desc);

        var view = new VulkanImageView(device, desc);
        this.views.put(desc, view);
        return view;
    }

    public void upload(Pixels pixels) {
        var alloc = new AutoHeapAllocator();

        GeneralBuffer cpuBuf = new GeneralBuffer(pixels.argbToByteBuffer(alloc), true);
        StagedBuffer buf = new StagedBuffer(device.getEngine(), device, cpuBuf,
                BufferUsage.Bits.TRANSFER_SRC.into(), MemoryUsage.Bits.CPU_TO_GPU.into());

        device.getRenderer().immediateSubmit((stack, cmd) -> {
            transition(cmd, ImageState.TRANSFER_DST);

            return buf.uploadViaStaging(() -> {}, stack, cmd);
        });

        device.getRenderer().immediateSubmit((stack, cmd) -> {
            cmd.copyBufferToImage(buf.getGpuBuffer(), this, 0, 0);
            transition(cmd, ImageState.FRAGMENT_SHADER_READ);
        });

        // TODO: generate mips

        alloc.close();
    }

    // region Transitions
    public void transition(VulkanCmdBuffers cmd, ImageState newState) {
        transition(cmd, newState, 0, desc.mipLevels, 0, desc.arrayLayers);
    }

    public void transition(VulkanCmdBuffers cmd, ImageState newState, int baseMip, int mipCount) {
        transition(cmd, newState, baseMip, mipCount, 0, desc.arrayLayers);
    }

    public void transition(VulkanCmdBuffers cmd, ImageState newState, int baseMip, int mipCount, int baseLayer, int layerCount) {
        transition(cmd, new VulkanImageBarrier(
                state.getStageMask(),
                newState.getStageMask(),
                state.getAccessMask(),
                newState.getAccessMask(),
                state.getLayout(),
                newState.getLayout(),
                baseMip,
                mipCount,
                baseLayer,
                layerCount,
                aspect), newState);
    }

    public void transition(VulkanCmdBuffers cmd, VulkanImageBarrier barrier, ImageState newState) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkImageSubresourceRange range = VkImageSubresourceRange.calloc(stack)
                    .aspectMask(barrier.aspect().getVkHandle())
                    .baseMipLevel(barrier.baseMip())
                    .levelCount(barrier.mipCount())
                    .baseArrayLayer(barrier.baseLayer())
                    .layerCount(barrier.layerCount());


            VkImageMemoryBarrier2.Buffer barriers = VkImageMemoryBarrier2.calloc(1, stack);
            barriers.get(0)
                    .sType$Default()
                    .srcStageMask(barrier.srcStage())
                    .srcAccessMask(barrier.srcAccess())
                    .dstStageMask(barrier.dstStage())
                    .dstAccessMask(barrier.dstAccess())
                    .oldLayout(barrier.oldLayout().getVkHandle())
                    .newLayout(barrier.newLayout().getVkHandle())
                    .srcQueueFamilyIndex(barrier.srcQueueFamily())
                    .dstQueueFamilyIndex(barrier.dstQueueFamily())
                    .image(this.handle)
                    .subresourceRange(range);

            VkDependencyInfo dependencyInfo = VkDependencyInfo.calloc(stack)
                    .sType$Default()
                    .dependencyFlags(0)
                    .pImageMemoryBarriers(barriers);

            VK14.vkCmdPipelineBarrier2(cmd.getBuffer(), dependencyInfo);
            this.state = newState;
        }
    }
    // endregion

    public ImageAspect getAspect() {
        return aspect;
    }

    @Override
    public TextureDesc description() {
        return desc;
    }

    @Override
    public void useInShader(FrameContext ctx) {
        transition((VulkanCmdBuffers) ctx.getCommandBuffer(), ImageState.FRAGMENT_SHADER_READ);
    }

    public long getHandle() {
        return handle;
    }

    public ImageState getState() {
        return state;
    }

    @Override
    public void free() {
        views.values().forEach(Disposable::free);
        if (allocation != 0)
            Vma.vmaDestroyImage(device.getVmaAllocator(), handle, allocation);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        VulkanTexture that = (VulkanTexture) o;
        return handle == that.handle && allocation == that.allocation && Objects.equals(desc, that.desc) && state == that.state && Objects.equals(aspect, that.aspect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(desc, handle, allocation, state, aspect);
    }
}
