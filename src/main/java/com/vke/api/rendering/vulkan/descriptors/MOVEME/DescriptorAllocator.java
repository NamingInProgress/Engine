package com.vke.api.rendering.vulkan.descriptors.MOVEME;

import com.carrotsearch.hppc.ObjectIntHashMap;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.utils.io.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;

import java.nio.LongBuffer;

public class DescriptorAllocator implements Disposable {

    private final VKEngine engine;
    private final VulkanRenderDevice device;
    private final DescriptorPool pool;

    public DescriptorAllocator(VKEngine engine, VulkanRenderDevice device, ObjectIntHashMap<DescriptorType> counts, int numSets) {
        this.engine = engine;
        this.device = device;

        this.pool = new DescriptorPool(engine, device, counts, numSets);
    }

    public long allocate(CompiledDescriptorSetLayout layout) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorSetAllocateInfo info = VkDescriptorSetAllocateInfo.calloc(stack)
                    .sType$Default()
                    .descriptorPool(pool.getHandle())
                    .pSetLayouts(stack.longs(layout.getHandle()));

            LongBuffer pSet = stack.mallocLong(1);
            if (VK14.vkAllocateDescriptorSets(device.getLogicalDevice().getDevice(), info, pSet) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create descriptor set!"), "DescriptorAllocator");
            }

            return pSet.get(0);
        }
    }

    @Override
    public void free() {

    }

}
