package com.vke.core.rendering.vulkan.descriptor;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.vke.api.vulkan.descriptors.DescriptorData;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.VulkanSetup;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.utils.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

public class DescriptorSet implements Disposable {

    // TODO: ADD ARRAYS TO BOTH HIGH AND LOW LEVEL ABSTRACTIONS
    // dstArrayElement on write thingy -> which element out of float[4] for example
    // descriptorCount -> how many of the floats writing with the pBuffer/pImage

    private long handle;

    private final IntObjectHashMap<DescriptorBinding> bindings = new IntObjectHashMap<>();

    private final LogicalDevice device;

    public DescriptorSet(long handle, VKEngine engine, VulkanSetup setup, LogicalDevice device, DescriptorSetLayout layout) {
        this.handle = handle;
        this.device = device;

        for (IntObjectCursor<DescriptorData.Binding> cursor : layout.getBindings()) {
            bindings.put(cursor.key, DescriptorBinding.fromType(engine, setup, cursor.value));
        }

        bindAll();
    }

    private void bindAll() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            for (IntObjectCursor<DescriptorBinding> cursor : bindings) {
                DescriptorBinding binding = cursor.value;
                if (binding.getType().isBuffer()) {
                    updateBuffer(stack, cursor.key, binding, binding.getBindingInfo(stack));
                }
            }
        }
    }

    public DescriptorBinding getBinding(int index) {
        return bindings.get(index);
    }

    public long getHandle() {
        return handle;
    }

    public void updateBuffer(MemoryStack stack, int bindingIndex, DescriptorBinding binding, VkDescriptorBufferInfo.Buffer info) {
        VkWriteDescriptorSet.Buffer writes = VkWriteDescriptorSet.calloc(1, stack);
        writes.get(0)
                .sType$Default()
                .dstSet(this.handle)
                .dstBinding(bindingIndex)
                .descriptorCount(1) // TODO: Change me later
                .descriptorType(binding.getType().getVkHandle())
                .pBufferInfo(info);

        VK14.vkUpdateDescriptorSets(device.getDevice(), writes, null);
    }

    public void updateImage(MemoryStack stack, int bindingIndex, DescriptorBinding binding, VkDescriptorImageInfo.Buffer info) {
        VkWriteDescriptorSet.Buffer writes = VkWriteDescriptorSet.calloc(1, stack);
        writes.get(0)
                .sType$Default()
                .dstSet(this.handle)
                .dstBinding(bindingIndex)
                .descriptorCount(1) // TODO: Change me later
                .descriptorType(binding.getType().getVkHandle())
                .pImageInfo(info);

        VK14.vkUpdateDescriptorSets(device.getDevice(), writes, null);
    }

    @Override
    public void free() {
        for (IntObjectCursor<DescriptorBinding> binding : bindings) {
            binding.value.free();
        }
    }
}
