package com.vke.core.vulkan.descriptor;

import com.carrotsearch.hppc.ObjectIntHashMap;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.utils.io.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetVariableDescriptorCountAllocateInfo;

import java.nio.LongBuffer;

public class DescriptorAllocator implements Disposable {

    private final VKEngine engine;
    private final VulkanRenderDevice device;
    private final DescriptorPool pool;

    public DescriptorAllocator(VKEngine engine, VulkanRenderDevice device, ObjectIntHashMap<DescriptorType> counts,
                               int numSets, int framesInFlight, boolean updateAfterBind) {
        this.engine = engine;
        this.device = device;

        this.pool = new DescriptorPool(engine, device, counts, numSets, framesInFlight, updateAfterBind);
    }

    public long allocate(CompiledDescriptorSetLayout layout) {
        return this.allocate(layout, -1);
    }

    public long allocate(CompiledDescriptorSetLayout layout, int descriptorCounts) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorSetAllocateInfo info = VkDescriptorSetAllocateInfo.calloc(stack)
                    .sType$Default()
                    .descriptorPool(pool.getHandle())
                    .pSetLayouts(stack.longs(layout.getHandle()));

            if (descriptorCounts != -1) {
                VkDescriptorSetVariableDescriptorCountAllocateInfo countInfo = VkDescriptorSetVariableDescriptorCountAllocateInfo.calloc(stack)
                        .sType$Default()
                        .pDescriptorCounts(stack.ints(descriptorCounts));
                info.pNext(countInfo);
            }

            LongBuffer pSet = stack.mallocLong(1);
            int err = VK14.vkAllocateDescriptorSets(device.getLogicalDevice().getDevice(), info, pSet);
            if (err != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create descriptor set! " + err), "DescriptorAllocator");
            }

            return pSet.get(0);
        }
    }

    @Override
    public void free() {
        this.pool.free();
    }

}
