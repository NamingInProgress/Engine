package com.vke.core.rendering.vulkan.descriptor;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.ObjectIntHashMap;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.rendering.vulkan.shader.Shader;
import com.vke.utils.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Struct;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

public class DescriptorSetLayout implements Disposable {
    private final LogicalDevice device;
    private final long handle;
    private final ObjectIntHashMap<DescriptorType> typeCounts;
    private final IntObjectHashMap<DescriptorType> layout = new IntObjectHashMap<>();

    public DescriptorSetLayout(VKEngine engine, DescriptorSetLayoutCreateInfo ci, ObjectIntHashMap<DescriptorType> typeCounts) {
        this.typeCounts = typeCounts;
        this.device = ci.device;

        ci.bindings.forEach(binding -> {
            layout.put(binding.binding(), DescriptorType.UniformBuffer.fromVkHandle(binding.descriptorType()));
        });

        try(MemoryStack stack = MemoryStack.stackPush()) {
            int size = ci.bindings.size();
            VkDescriptorSetLayoutBinding.Buffer buf = VkDescriptorSetLayoutBinding.calloc(size, stack);
            for (int i = 0; i < size; i++) {
                buf.get(i)
                        .set(ci.bindings.get(i));
            }

            VkDescriptorSetLayoutCreateInfo createInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack);
            createInfo.sType$Default();
            createInfo.pBindings(buf);

            LongBuffer pLayout = stack.mallocLong(1);
            int VK_RESULT = VK14.vkCreateDescriptorSetLayout(device.getDevice(), createInfo, null, pLayout);
            if (VK_RESULT != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create descriptor set layout!"), "Descriptor Set Layout");
            }

            handle = pLayout.get(0);
        }
    }

    public long getHandle() {
        return handle;
    }

    public ObjectIntHashMap<DescriptorType> typeCounts() {
        return typeCounts;
    }

    public IntObjectHashMap<DescriptorType> layout() { return layout; }

    @Override
    public void free() {
        VK14.vkDestroyDescriptorSetLayout(device.getDevice(), handle, null);
    }

    public static class Builder {
        private final List<VkDescriptorSetLayoutBinding> bindings;
        private final ObjectIntHashMap<DescriptorType> typeCounts;

        public Builder() {
            bindings = new ArrayList<>();
            typeCounts = new ObjectIntHashMap<>();
        }

        public Builder addBinding(int index, DescriptorType type, Shader.Stages shaderStageFlags) {
            VkDescriptorSetLayoutBinding b = VkDescriptorSetLayoutBinding.calloc();
            b.binding(index);
            b.descriptorType(type.getVkHandle());
            b.stageFlags(shaderStageFlags.getVkHandle());
            bindings.add(b);
            typeCounts.addTo(type, 1);
            return this;
        }

        public DescriptorSetLayout build(VKEngine engine, LogicalDevice device) {
            DescriptorSetLayoutCreateInfo ci = new DescriptorSetLayoutCreateInfo();
            ci.device = device;
            ci.bindings = bindings;
            var layout = new DescriptorSetLayout(engine, ci, typeCounts);
            bindings.forEach(Struct::free);
            return layout;
        }
    }
}
