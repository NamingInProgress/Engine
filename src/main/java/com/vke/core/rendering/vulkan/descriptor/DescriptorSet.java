package com.vke.core.rendering.vulkan.descriptor;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.vke.api.vulkan.pipeline.Descriptor;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.util.function.Consumer;

public class DescriptorSet {

    private long handle;
    private int index;

    private final LogicalDevice device;

    private final IntObjectHashMap<Descriptor> descriptors = new IntObjectHashMap<>();

    public DescriptorSet(long handle, LogicalDevice device, DescriptorSetLayout layout) {
        this.handle = handle;
        this.device = device;

        layout.layout().forEach((Consumer<? super IntObjectCursor<DescriptorType>>) (cursor) -> {
            descriptors.put(cursor.key, Descriptor.fromType(cursor.value, cursor.key, this));
        });
    }

    public Descriptor getBuffer(int binding) {
        return this.descriptors.get(binding);
    }

    public void updateBuffer(MemoryStack stack, Descriptor d, VkDescriptorBufferInfo.Buffer info) {
        VkWriteDescriptorSet.Buffer writes = VkWriteDescriptorSet.calloc(1, stack);
        writes.get(0)
                .sType$Default()
                .dstBinding(d.binding)
                .dstSet(this.handle)
                .descriptorCount(1)
                .descriptorType(d.type.getVkHandle())
                .pBufferInfo(info);

        VK14.vkUpdateDescriptorSets(device.getDevice(), writes, null);
    }

    public void updateImage(MemoryStack stack, Descriptor d, VkDescriptorImageInfo.Buffer info) {
        VkWriteDescriptorSet.Buffer writes = VkWriteDescriptorSet.calloc(1, stack);
        writes.get(0)
                .sType$Default()
                .dstBinding(d.binding)
                .dstSet(this.handle)
                .descriptorCount(1)
                .descriptorType(d.type.getVkHandle())
                .pImageInfo(info);

        VK14.vkUpdateDescriptorSets(device.getDevice(), writes, null);
    }

    public long getHandle() { return this.handle; }
}
