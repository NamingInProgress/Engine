package com.vke.api.vulkan.pipeline;

import com.vke.api.vulkan.buffer.CpuBuffer;
import com.vke.core.rendering.vulkan.descriptor.DescriptorSet;
import com.vke.core.rendering.vulkan.descriptor.DescriptorType;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;

public abstract class Descriptor {

    public int binding;
    public DescriptorType type;
    protected DescriptorSet descriptorSet;

    public Descriptor(int binding, DescriptorType type, DescriptorSet descriptorSet) {
        this.binding = binding;
        this.type = type;
        this.descriptorSet = descriptorSet;
    }

    public abstract void write(CpuBuffer buf);

    public static Descriptor fromType(DescriptorType type, int binding, DescriptorSet set) {
        return switch (type) {
            case UniformBuffer -> new UniformBuffer(binding, type, set);
            case StorageBuffer -> new UniformBuffer(binding, type, set);
            case CombinedImageSampler -> new UniformBuffer(binding, type, set);
            case StorageImage -> new UniformBuffer(binding, type, set);
        };
    }

    public static class UniformBuffer extends Descriptor {

        public UniformBuffer(int binding, DescriptorType type, DescriptorSet descriptorSet) {
            super(binding, type, descriptorSet);
        }

        @Override
        public void write(CpuBuffer buf) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                VkDescriptorBufferInfo.Buffer info = VkDescriptorBufferInfo.calloc(1, stack);
                info.get(0)
                        .

                this.descriptorSet.updateBuffer(stack, binding, info);
            }
        }

    }

}
