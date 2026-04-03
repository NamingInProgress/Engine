package com.vke.api.rendering.vulkan.descriptors.MOVEME;

import com.carrotsearch.hppc.ObjectIntHashMap;
import com.carrotsearch.hppc.cursors.ObjectIntCursor;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.utils.io.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;

import java.nio.LongBuffer;

public class DescriptorPool implements Disposable {

    private final long handle;
    private final VulkanRenderDevice device;

    public DescriptorPool(VKEngine engine, VulkanRenderDevice device, ObjectIntHashMap<DescriptorType> counts, int numSets) {
        this.device = device;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorPoolSize.Buffer countsBuffer = VkDescriptorPoolSize.calloc(counts.size(), stack);

            int index = 0;
            for (ObjectIntCursor<DescriptorType> countCursor : counts) {
                countsBuffer.get(index++)
                        .type(countCursor.key.getVkHandle())
                        .descriptorCount(countCursor.value);
            }

            VkDescriptorPoolCreateInfo poolCreateInfo = VkDescriptorPoolCreateInfo.calloc(stack)
                    .sType$Default()
                    .maxSets(numSets)
                    .pPoolSizes(countsBuffer);

            LongBuffer pPool = stack.mallocLong(1);
            if (VK14.vkCreateDescriptorPool(device.getLogicalDevice().getDevice(), poolCreateInfo, null, pPool) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create descriptor pool!"), "Descriptor Pool");
            }

            this.handle = pPool.get(0);
        }
    }

    public long getHandle() {
        return handle;
    }

    public void reset() {
        VK14.vkResetDescriptorPool(device.getLogicalDevice().getDevice(), handle, 0);
    }

    @Override
    public void free() {
        VK14.vkDestroyDescriptorPool(device.getLogicalDevice().getDevice(), handle, null);
    }

}
