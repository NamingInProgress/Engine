package com.vke.core.vulkan.texture;

import com.vke.api.abstraction.data.Buffer;
import com.vke.api.abstraction.data.Texture;
import com.vke.api.abstraction.data.TextureView;
import com.vke.api.abstraction.descriptors.buffer.BufferUsage;
import com.vke.api.abstraction.descriptors.buffer.MemoryUsage;
import com.vke.api.abstraction.descriptors.texture.ImageUsage;
import com.vke.api.abstraction.descriptors.texture.TextureFormat;
import com.vke.api.vulkan.buffer.CpuBuffer;
import com.vke.core.VKEngine;
import com.vke.core.rendering.imageloading.ImageData;
import com.vke.core.rendering.imageloading.LowLevelImageLoader;
import com.vke.core.services.Services;
import com.vke.core.vulkan.VKUtils;
import com.vke.core.vulkan.VulkanRenderer;
import com.vke.core.vulkan.buffers.GpuBuffer;
import com.vke.core.vulkan.buffers.StagedBuffer;
import com.vke.core.vulkan.buffers.premade.GeneralBuffer;
import com.vke.core.vulkan.extent.Extent3D;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.extent.VulkanExtentUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;

public class VulkanTexture implements Texture {

    private final long image;
    private final long allocation;
    private final Extent3D extent;
    private final TextureFormat format;

    private VulkanTextureView view;

    private final VulkanRenderDevice device;

    public VulkanTexture(VulkanRenderDevice device, Description info, MemoryUsage memUsage) {
        this(device, info.format(), info.extent(), info.usageFlags(), memUsage);
    }

    private VulkanTexture(VulkanRenderDevice device, TextureFormat format, Extent3D extent, ImageUsage imageUsageFlags, MemoryUsage memoryUsage) {
        this.device = device;
        this.format = format;
        this.extent = extent;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkImageCreateInfo imageCreateInfo = getDefaultImageCreateInfo(stack, format, imageUsageFlags, VulkanExtentUtils.createVk3D(stack, extent));
            VmaAllocationCreateInfo allocInfo = VmaAllocationCreateInfo.calloc(stack)
                    .usage(memoryUsage.getVkHandle())
                    .requiredFlags(VK14.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);

            LongBuffer pImage = stack.mallocLong(1);
            PointerBuffer pAllocation = stack.mallocPointer(1);
            Vma.vmaCreateImage(device.getVmaAllocator(), imageCreateInfo, allocInfo, pImage, pAllocation, null);
            this.image = pImage.get(0);
            this.allocation = pAllocation.get(0);
        }
    }

    public static VkImageCreateInfo getDefaultImageCreateInfo(MemoryStack stack, TextureFormat format, ImageUsage usageFlags, VkExtent3D extent) {
        return VkImageCreateInfo.calloc(stack)
                .sType$Default()
                .imageType(VK14.VK_IMAGE_TYPE_2D)
                .format(format.getVkHandle())
                .extent(extent)
                .mipLevels(1)
                .arrayLayers(1)
                .samples(VK14.VK_SAMPLE_COUNT_1_BIT)
                .tiling(VK14.VK_IMAGE_TILING_OPTIMAL)
                .usage(usageFlags.getVkHandle());
    }

    public long getHandle() { return this.image; }

    @Override
    public void free() {
        if (view != null)
            view.free();
        Vma.vmaDestroyImage(device.getVmaAllocator(), image, allocation);
    }

    @Override
    public void loadImage(InputStream inputStream) {
        VKEngine engine = device.getEngine();
        VulkanRenderer renderer = engine.service(Services.VULKAN_RENDERER);

        int educatedGuess = 4096;

        GeneralBuffer data = null;
        try {
            data = VKUtils.readInputStreamToVulkanAndClose(inputStream, educatedGuess);
        } catch (IOException e) {
            engine.throwException(e, "VulkanTexture#loadImage");
        }
        LowLevelImageLoader llil = new LowLevelImageLoader(data.getData());

        ImageData image = llil.decode(engine);
        ByteBuffer imageData = image.pixels();
        long imageSize = (long) image.width() * image.height() * 4;
        GpuBuffer uploadBuffer = new GpuBuffer(engine, device, new Buffer.Description(
                imageSize, BufferUsage.Bits.TRANSFER_SRC.into(),
                MemoryUsage.Bits.CPU_TO_GPU.into())
        );
        long uploadAddress = uploadBuffer.getInfo().pMappedData();
        long srcAddress = MemoryUtil.memAddress(imageData);
        MemoryUtil.memCopy(srcAddress, uploadAddress, imageSize);

        renderer.immediateSubmit((stack, cmd) -> {

        });

        uploadBuffer.free();
    }

    @Override
    public int width() {
        return extent.width;
    }

    @Override
    public int height() {
        return extent.height;
    }

    @Override
    public int depth() {
        return extent.depth;
    }

    @Override
    public TextureFormat format() {
        return format;
    }

    @Override
    public int mipLevels() {
        return 0;
    }

    @Override
    public boolean isSwapchainImage() {
        return false;
    }

    @Override
    public TextureView createView(TextureView.Description info) {
        if (view != null) return view;
        this.view = new VulkanTextureView(device.getLogicalDevice(), info);
        return this.view;
    }
}
