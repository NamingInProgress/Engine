package com.vke.core.vulkan.descriptor;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.vke.api.abstraction.IntEnum;
import com.vke.api.pipeline.DescriptorData;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.device.LogicalDevice;
import com.vke.core.vulkan.shader.Shader;
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
    private final IntObjectHashMap<DescriptorData.Binding> binding;

    private final IntObjectHashMap<DescriptorType> layout = new IntObjectHashMap<>();

    public DescriptorSetLayout(VKEngine engine, DescriptorSetLayoutCreateInfo ci, IntObjectHashMap<DescriptorData.Binding> b) {
        this.device = ci.device;
        this.binding = b;

        ci.bindings.forEach(binding -> {
            layout.put(binding.binding(), IntEnum.fromInt(DescriptorType.values(), binding.descriptorType()));
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

    public IntObjectHashMap<DescriptorData.Binding> getBindings() {
        return binding;
    }

    @Override
    public void free() {
        VK14.vkDestroyDescriptorSetLayout(device.getDevice(), handle, null);
    }

    public static class Builder {
        private final List<VkDescriptorSetLayoutBinding> bindings;
        private IntObjectHashMap<DescriptorData.Binding> wrappers;

        public Builder() {
            bindings = new ArrayList<>();
        }

        private Builder addBinding(int index, DescriptorType type, Shader.Stages shaderStageFlags) {
            VkDescriptorSetLayoutBinding b = VkDescriptorSetLayoutBinding.calloc();
            b.binding(index);
            b.descriptorCount(1); // TODO: vchange when adding arrays
            b.descriptorType(type.getVkHandle());
            b.stageFlags(shaderStageFlags.getVkHandle());
            bindings.add(b);
            return this;
        }

        public void fromWrapper(IntObjectHashMap<DescriptorData.Binding> bindings) {
            wrappers = bindings;

            for (IntObjectCursor<DescriptorData.Binding> binding : bindings) {
                addBinding(binding.key, DescriptorType.fromWrapper(binding.value.getType()), binding.value.getStages());
            }
        }

        public DescriptorSetLayout build(VKEngine engine, LogicalDevice device) {
            DescriptorSetLayoutCreateInfo ci = new DescriptorSetLayoutCreateInfo();
            ci.device = device;
            ci.bindings = bindings;
            var layout = new DescriptorSetLayout(engine, ci, wrappers);
            bindings.forEach(Struct::free);
            return layout;
        }
    }
}
