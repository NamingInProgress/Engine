package com.vke.core.vulkan.descriptor;

import com.vke.api.vulkan.ImageLayout;
import com.vke.api.pipeline.DescriptorData;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.buffers.premade.BufferSlice;
import com.vke.core.vulkan.buffers.MappedBuffer;
import com.vke.api.abstraction.descriptors.buffer.BufferUsage;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.sampler.VulkanSampler;
import com.vke.core.vulkan.texture.VulkanTexture;
import com.vke.core.vulkan.texture.VulkanTextureView;
import com.vke.utils.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.StructBuffer;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;

import java.util.function.Consumer;

public abstract class DescriptorBinding implements Disposable {

    private final DescriptorType type;
    private final VKEngine engine;
    private final VulkanRenderDevice device;

    public DescriptorBinding(VKEngine engine, VulkanRenderDevice device, DescriptorType type) {
        this.type = type;
        this.engine = engine;
        this.device = device;
    }

    public DescriptorType getType() {
        return type;
    }

    public static DescriptorBinding fromType(VKEngine engine, VulkanRenderDevice device, DescriptorData.Binding binding) {
        DescriptorType type = DescriptorType.fromWrapper(binding.getType());
        return switch (type) {
            case CombinedImageSampler -> new SamplerBinding(engine, device, type);
            case StorageImage -> new ImageBinding(engine, device, type);
            case UniformBuffer, StorageBuffer -> new BufferBinding(engine, device, type, binding.getStruct(), binding.getStruct().sizeof());
        };
    }

    public abstract <T extends StructBuffer<?, ?>> T getBindingInfo(MemoryStack stack);

    public static class ImageBinding extends DescriptorBinding {

        public long imageView;
        public ImageLayout layout;

        public ImageBinding(VKEngine engine, VulkanRenderDevice device, DescriptorType type) {
            super(engine, device, type);
        }

        public void setImageView(long handle) {
            this.imageView = handle;
        }

        public void setImageLayout(ImageLayout layout) {
            this.layout = layout;
        }

        @Override
        public <T extends StructBuffer<?, ?>> T getBindingInfo(MemoryStack stack) {
            VkDescriptorImageInfo.Buffer info = VkDescriptorImageInfo.calloc(1, stack);

            info.get(0)
                    .sampler(VK14.VK_NULL_HANDLE)
                    .imageView(this.imageView)
                    .imageLayout(layout.getVkHandle());

            return (T) info;
        }

        @Override
        public void free() {}
    }

    public static class SamplerBinding extends DescriptorBinding {

        public VulkanSampler sampler;
        public VulkanTextureView imageView;
        public ImageLayout layout;

        public SamplerBinding(VKEngine engine, VulkanRenderDevice device, DescriptorType type) {
            super(engine, device, type);
        }

        public void setSampler(VulkanSampler sampler) {
            this.sampler = sampler;
        }

        public void setImageView(VulkanTextureView view) {
            this.imageView = view;
            setImageLayout(view.image.layout());
        }

        public void setImageView(VulkanTexture tex) {
            this.setImageView(tex.getView());
        }

        public void setImageLayout(ImageLayout layout) {
            this.layout = layout;
        }

        @Override
        public <T extends StructBuffer<?, ?>> T getBindingInfo(MemoryStack stack) {
            VkDescriptorImageInfo.Buffer info = VkDescriptorImageInfo.calloc(1, stack);

            info.get(0)
                    .sampler(this.sampler.getHandle())
                    .imageView(this.imageView.getHandle())
                    .imageLayout(layout.getVkHandle());

            return (T) info;
        }

        @Override
        public void free() {}
    }

    public static class BufferBinding extends DescriptorBinding {

        private final MappedBuffer buffer;
        private final long size;
        private final DescriptorData.Struct struct;

        // Assumes size is byte aligned size
        public BufferBinding(VKEngine engine, VulkanRenderDevice device, DescriptorType type, DescriptorData.Struct struct, long size) {
            super(engine, device, type);
            this.struct = struct;
            
            BufferUsage usage = new BufferUsage(type == DescriptorType.StorageBuffer ? BufferUsage.Bits.SSBO : BufferUsage.Bits.UBO);

            buffer = new MappedBuffer(engine, device, size, usage);
            this.size = size;
        }

        public void write(String name, Consumer<BufferSlice> consumer) {
            DescriptorData.Entry entry = struct.byName(name);
            long preceding = struct.preceding(entry);
            consumer.accept(new BufferSlice(buffer, preceding, entry.getSize()));
        }

        public <T extends StructBuffer<?, ?>> T getBindingInfo(MemoryStack stack) {
            VkDescriptorBufferInfo.Buffer info = VkDescriptorBufferInfo.calloc(1, stack);

            info.get(0)
                    .buffer(buffer.getGpuBuffer().getBuffer())
                    .offset(0)
                    .range(size);

            //for (int i = 0; i < buffer.length; i++) {
            //    info.get(i)
            //            .buffer(buffer[i].getGpuBuffer().getBuffer())
            //            .offset(sameDataForAll ? 0 : i * size)
            //            .range(size);
            //} //TODO: dynamic ranges

            return (T) info;
        }

        @Override
        public void free() {
            buffer.free();
        }
        
    }

}
