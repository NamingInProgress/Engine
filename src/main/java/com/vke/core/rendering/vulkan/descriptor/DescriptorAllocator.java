package com.vke.core.rendering.vulkan.descriptor;

import com.carrotsearch.hppc.ObjectIntHashMap;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import com.vke.utils.io.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetVariableDescriptorCountAllocateInfo;

import java.nio.LongBuffer;

public class DescriptorAllocator implements Disposable {

    private final DescriptorPool pool;
    private final VulkanRenderSystem ctx;

    public DescriptorAllocator(VulkanRenderSystem ctx, ObjectIntHashMap<DescriptorType> counts,
                               int numSets, int framesInFlight, boolean updateAfterBind) {

        this.pool = new DescriptorPool(ctx, counts, numSets, framesInFlight, updateAfterBind);
        this.ctx = ctx;
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
            int err = VK14.vkAllocateDescriptorSets(ctx.device().vkLogicalDevice(), info, pSet);
            if (err != VK14.VK_SUCCESS) {
                ctx.throwException(new IllegalStateException("Failed to create descriptor set! " + err), "DescriptorAllocator");
            }

            return pSet.get(0);
        }
    }

    @Override
    public void free() {
        this.pool.free();
    }

}
