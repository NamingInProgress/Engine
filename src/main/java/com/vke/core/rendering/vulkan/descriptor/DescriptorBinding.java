package com.vke.core.rendering.vulkan.descriptor;

import com.vke.api.vulkan.descriptors.DescriptorData;
import com.vke.core.VKEngine;
import com.vke.core.rendering.buffer.BufferSlice;
import com.vke.core.rendering.vulkan.VulkanSetup;
import com.vke.core.rendering.vulkan.buffer.MappedBuffer;
import com.vke.core.rendering.vulkan.mem.GpuBuffer;
import com.vke.utils.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.StructBuffer;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;

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
            case CombinedImageSampler -> null;
            case StorageImage -> null;
            case UniformBuffer -> new BufferBinding(engine, setup, type, binding.getStruct(), binding.getStruct().sizeof(), 1);
            case StorageBuffer -> null;
        };
    }

    public abstract <T extends StructBuffer<?, ?>> T getBindingInfo(MemoryStack stack);
    
    public static class BufferBinding extends DescriptorBinding {

        private final MappedBuffer[] buffers;
        private final boolean sameDataForAll;
        private final long size;
        private final DescriptorData.Struct struct;

        public BufferBinding(VKEngine engine, VulkanSetup setup, DescriptorType type, DescriptorData.Struct struct, long size, int amount) {
            this(engine, setup, type, struct, size, amount, false);
        }

        // Assumes size is byte aligned size
        public BufferBinding(VKEngine engine, VulkanSetup setup, DescriptorType type, DescriptorData.Struct struct, long size, int amount, boolean sameDataForAll) {
            super(engine, setup, type);
            this.struct = struct;
            
            GpuBuffer.BufferUsage usage = new GpuBuffer.BufferUsage(type == DescriptorType.StorageBuffer ? GpuBuffer.BufferUsage.Bits.SSBO : GpuBuffer.BufferUsage.Bits.UBO);

            buffers = new MappedBuffer[amount];

            for (int i = 0; i < buffers.length; i++) {
                buffers[i] = new MappedBuffer(engine, setup, size, usage);
            }

            this.sameDataForAll = sameDataForAll;
            this.size = size;
        }

        public void write(String name, Consumer<BufferSlice> consumer) {
            DescriptorData.Entry entry = struct.byName(name);
            long preceding = struct.preceding(entry);
            consumer.accept(new BufferSlice(buffers[0], preceding, entry.getSize()));
        }

        public <T extends StructBuffer<?, ?>> T getBindingInfo(MemoryStack stack) {
            VkDescriptorBufferInfo.Buffer info = VkDescriptorBufferInfo.calloc(buffers.length, stack);

            for (int i = 0; i < buffers.length; i++) {
                info.get(i)
                        .buffer(buffers[i].getGpuBuffer().getBuffer())
                        .offset(sameDataForAll ? 0 : i * size)
                        .range(size);
            }

            return (T) info;
        }

        @Override
        public void free() {
            for (MappedBuffer buffer : buffers) {
                buffer.free();
            }
        }
        
    }

}
