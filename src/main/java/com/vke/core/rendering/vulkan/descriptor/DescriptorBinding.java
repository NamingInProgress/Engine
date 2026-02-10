package com.vke.core.rendering.vulkan.descriptor;

import com.vke.api.vulkan.ImageLayout;
import com.vke.api.vulkan.descriptors.DescriptorData;
import com.vke.core.VKEngine;
import com.vke.core.rendering.buffer.BufferSlice;
import com.vke.core.rendering.vulkan.VulkanSetup;
import com.vke.core.rendering.vulkan.buffer.MappedBuffer;
import com.vke.core.rendering.vulkan.mem.GpuBuffer;
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
    private final VulkanSetup setup;

    public DescriptorBinding(VKEngine engine, VulkanSetup setup, DescriptorType type) {
        this.type = type;
        this.engine = engine;
        this.setup = setup;
    }

    public DescriptorType getType() {
        return type;
    }

    public static DescriptorBinding fromType(VKEngine engine, VulkanSetup setup, DescriptorData.Binding binding) {
        DescriptorType type = DescriptorType.fromWrapper(binding.getType());
        return switch (type) {
            case CombinedImageSampler -> new SamplerBinding(engine, setup, type);
            case StorageImage -> new ImageBinding(engine, setup, type);
            case UniformBuffer, StorageBuffer -> new BufferBinding(engine, setup, type, binding.getStruct(), binding.getStruct().sizeof());
        };
    }

    public abstract <T extends StructBuffer<?, ?>> T getBindingInfo(MemoryStack stack);

    public static class ImageBinding extends DescriptorBinding {

        public long imageView;
        public ImageLayout layout;

        public ImageBinding(VKEngine engine, VulkanSetup setup, DescriptorType type) {
            super(engine, setup, type);
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

        public long sampler;
        public long imageView;
        public ImageLayout layout;

        public SamplerBinding(VKEngine engine, VulkanSetup setup, DescriptorType type) {
            super(engine, setup, type);
        }

        public void setSampler(long handle) {
            this.sampler = handle;
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
                    .sampler(this.sampler)
                    .imageView(this.imageView)
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
        public BufferBinding(VKEngine engine, VulkanSetup setup, DescriptorType type, DescriptorData.Struct struct, long size) {
            super(engine, setup, type);
            this.struct = struct;
            
            GpuBuffer.BufferUsage usage = new GpuBuffer.BufferUsage(type == DescriptorType.StorageBuffer ? GpuBuffer.BufferUsage.Bits.SSBO : GpuBuffer.BufferUsage.Bits.UBO);

            buffer = new MappedBuffer(engine, setup, size, usage);
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
