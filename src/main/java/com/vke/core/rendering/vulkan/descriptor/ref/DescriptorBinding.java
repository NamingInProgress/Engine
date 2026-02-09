package com.vke.core.rendering.vulkan.descriptor.ref;

import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.VulkanSetup;
import com.vke.core.rendering.vulkan.buffer.MappedBuffer;
import com.vke.core.rendering.vulkan.descriptor.DescriptorType;
import com.vke.core.rendering.vulkan.mem.GpuBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.StructBuffer;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;

public abstract class DescriptorBinding {

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

    public static DescriptorBinding fromType(VKEngine engine, VulkanSetup setup, DescriptorType type) {
        return switch (type) {
            case UniformBuffer -> new BufferBinding(engine, setup, type, )
        }
    }

    public abstract StructBuffer<?, ?> getBindingInfo(MemoryStack stack);
    
    public static class BufferBinding extends DescriptorBinding {

        private final MappedBuffer[] buffers;
        private final boolean sameDataForAll;
        private final long size;

        public BufferBinding(VKEngine engine, VulkanSetup setup, DescriptorType type, long size, int amount) {
            this(engine, setup, type, size, amount, false);
        }

        // Assumes size is byte aligned size
        public BufferBinding(VKEngine engine, VulkanSetup setup, DescriptorType type, long size, int amount, boolean sameDataForAll) {
            super(engine, setup, type);
            
            GpuBuffer.BufferUsage usage = new GpuBuffer.BufferUsage(type == DescriptorType.StorageBuffer ? GpuBuffer.BufferUsage.Bits.SSBO : GpuBuffer.BufferUsage.Bits.UBO);

            buffers = new MappedBuffer[amount];

            for (int i = 0; i < buffers.length; i++) {
                buffers[i] = new MappedBuffer(engine, setup, size, usage);
            }

            this.sameDataForAll = sameDataForAll;
            this.size = size;
        }

        public VkDescriptorBufferInfo.Buffer getBindingInfo(MemoryStack stack) {
            VkDescriptorBufferInfo.Buffer info = VkDescriptorBufferInfo.calloc(buffers.length, stack);

            for (int i = 0; i < buffers.length; i++) {
                info.get(i)
                        .buffer(buffers[i].getGpuBuffer().getBuffer())
                        .offset(sameDataForAll ? 0 : i * size)
                        .range(size);
            }

            return info;
        }
        
    }

}
