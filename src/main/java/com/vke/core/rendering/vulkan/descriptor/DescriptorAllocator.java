package com.vke.core.rendering.vulkan.descriptor;

import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.utils.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;

import java.nio.LongBuffer;

public class DescriptorAllocator implements Disposable {
    private DescriptorPool pool;
    private LogicalDevice device;
    private VKEngine engine;

    public DescriptorAllocator(DescriptorPoolCreateInfo descriptorPoolCreateInfo) {
        this.device = descriptorPoolCreateInfo.logicalDevice;
        this.engine = descriptorPoolCreateInfo.engine;
        this.pool = new DescriptorPool(descriptorPoolCreateInfo);
    }

    public void resetDescriptors(LogicalDevice device) {
        VK14.vkResetDescriptorPool(device.getDevice(), pool.getHandle(), 0);
    }

    public DescriptorSet allocate(DescriptorSetLayout layout) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorSetAllocateInfo info = VkDescriptorSetAllocateInfo.calloc(stack)
                    .sType$Default()
                    .descriptorPool(pool.getHandle())
                    .pSetLayouts(stack.longs(layout.getHandle()));

            LongBuffer pDescriptorSet = stack.mallocLong(1);
            if (VK14.vkAllocateDescriptorSets(device.getDevice(), info, pDescriptorSet) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create descriptor set!"), "Descriptor Allocator");
            }
            DescriptorSet set = new DescriptorSet(pDescriptorSet.get(0), device, layout);
            return set;
        }
    }

    @Override
    public void free() {
        pool.free();
    }
}
