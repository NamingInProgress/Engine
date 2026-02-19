package com.vke.core.vulkan.texture;

import com.vke.api.abstraction.data.Buffer;
import com.vke.api.abstraction.data.Texture;
import com.vke.api.abstraction.descriptors.buffer.BufferUsage;
import com.vke.api.abstraction.descriptors.buffer.MemoryUsage;
import com.vke.api.abstraction.descriptors.texture.ImageAspect;
import com.vke.api.abstraction.descriptors.texture.TextureFormat;
import com.vke.api.abstraction.descriptors.texture.TextureType;
import com.vke.api.vulkan.ImageLayout;
import com.vke.core.VKEngine;
import com.vke.core.file.png.PngFile;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.rendering.imageloading.ImageData;
import com.vke.core.rendering.imageloading.LowLevelImageLoader;
import com.vke.core.services.Services;
import com.vke.core.vulkan.VulkanRenderer;
import com.vke.core.vulkan.buffers.GpuBuffer;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.extent.VulkanExtentUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkBufferImageCopy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class VulkanTexture implements Texture {

    private final VulkanImage image;

    private final AutoHeapAllocator alloc;

    private final VulkanRenderDevice device;

    public VulkanTexture(VulkanRenderDevice device, InputStream is, Texture.TextureDesc desc) {
        this.image = new VulkanImage(device, desc, MemoryUsage.Bits.GPU_ONLY.into());
        this.device = device;
        this.alloc = new AutoHeapAllocator();

        this.image.getView();

        loadImage(is);
    }

    private void loadImage(InputStream inputStream) {
        // TODO: Fix this for later support for arrays by adding offsets and shit
        try {
            VKEngine engine = device.getEngine();
            VulkanRenderer renderer = engine.service(Services.VULKAN_RENDERER);
            PngFile file = new PngFile(inputStream);

            ImageData image = new ImageData(file.getPngInfo().width, file.getPngInfo().height, file.getOutput().argbToByteBuffer(alloc));

            ByteBuffer imageData = image.pixels();
            long imageSize = (long) image.width() * image.height() * 4;
            GpuBuffer uploadBuffer = device.createBuffer(new Buffer.Description(
                    imageSize, BufferUsage.Bits.TRANSFER_SRC.into(),
                    MemoryUsage.Bits.CPU_TO_GPU.into()
            ));

            long uploadAddress = uploadBuffer.getInfo().pMappedData();
            long srcAddress = MemoryUtil.memAddress(imageData);
            MemoryUtil.memCopy(srcAddress, uploadAddress, imageSize);

            renderer.immediateSubmit((stack, cmd) -> {
                this.image.transitionLayout(cmd, ImageLayout.TRANSFER_DST_OPTIMAL);

                VkBufferImageCopy.Buffer copyRegion = VkBufferImageCopy.calloc(1, stack);
                copyRegion.get(0)
                        .bufferOffset(0)
                        .bufferRowLength(0)
                        .bufferImageHeight(0)
                        .imageSubresource((srs) -> {
                            srs.aspectMask(this.image.getView().aspectMask.getVkHandle())
                                    .mipLevel(0)
                                    .baseArrayLayer(0)
                                    .layerCount(this.image.arrayLayers);
                        })
                        .imageExtent(VulkanExtentUtils.createVk3D(stack, this.image.extent));

                VK14.vkCmdCopyBufferToImage(cmd.getBuffer(), uploadBuffer.getBuffer(), this.image.getHandle(), this.image.layout.getVkHandle(), copyRegion);

                this.image.transitionLayout(cmd, ImageLayout.SHADER_READONLY_OPTIMAL);
            }, uploadBuffer::free);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public VulkanTextureView getView() { return this.image.getView(); }

    @Override
    public int width() {
        return image.extent.width;
    }

    @Override
    public int height() {
        return image.extent.height;
    }

    @Override
    public int depth() {
        return image.extent.depth;
    }

    @Override
    public int mipLevels() {
        return image.mipLevels;
    }

    @Override
    public int arrayLayers() {
        return image.arrayLayers;
    }

    @Override
    public TextureType type() {
        return image.getType();
    }

    @Override
    public TextureFormat format() {
        return image.getFormat();
    }

    @Override
    public long getHandle() {
        return image.getHandle();
    }

    @Override
    public void free() {
        alloc.close();
        image.free();
    }
}
