package com.vke.core.rendering.vulkan.descriptor.ref;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.vke.api.vulkan.pipeline.Descriptor;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.VulkanSetup;
import com.vke.core.rendering.vulkan.descriptor.DescriptorSetLayout;
import com.vke.core.rendering.vulkan.descriptor.DescriptorType;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.util.function.Consumer;

public class DescriptorSet {

    // TODO: ADD ARRAYS TO BOTH HIGH AND LOW LEVEL ABSTRACTIONS
    // dstArrayElement on write thingy -> which element out of float[4] for example
    // descriptorCount -> how many of the floats writing with the pBuffer/pImage

    private final int index;

    private long handle;

    private final IntObjectHashMap<DescriptorBinding> bindings = new IntObjectHashMap<>();

    private final LogicalDevice device;

    public DescriptorSet(long handle, VKEngine engine, VulkanSetup setup, LogicalDevice device, int index, DescriptorSetLayout layout) {
        this.index = index;
        this.handle = handle;
        this.device = device;

        layout.layout().forEach((Consumer<? super IntObjectCursor<DescriptorType>>) (cursor) -> {
            bindings.put(cursor.key, DescriptorBinding.fromType(engine, setup, cursor.value));
        });
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

}
