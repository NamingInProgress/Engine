package com.vke.core.rendering.vulkan.descriptor;

import com.carrotsearch.hppc.ObjectIntHashMap;
import com.carrotsearch.hppc.cursors.ObjectIntCursor;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import com.vke.utils.io.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;

import java.nio.LongBuffer;

public class DescriptorPool implements Disposable {

    private final long handle;
    private final VulkanRenderSystem ctx;

    private boolean free;

    public DescriptorPool(VulkanRenderSystem ctx, ObjectIntHashMap<DescriptorType> counts, int numSets, int framesInFlight, boolean updateAfterBind) {
        this.ctx = ctx;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorPoolSize.Buffer countsBuffer = VkDescriptorPoolSize.calloc(counts.size(), stack);

            int index = 0;
            for (ObjectIntCursor<DescriptorType> countCursor : counts) {
                countsBuffer.get(index++)
                        .type(countCursor.key.getIntVal())
                        .descriptorCount(countCursor.value * framesInFlight);
            }

            VkDescriptorPoolCreateInfo poolCreateInfo = VkDescriptorPoolCreateInfo.calloc(stack)
                    .sType$Default()
                    .maxSets(numSets * framesInFlight)
                    .pPoolSizes(countsBuffer);

            if (updateAfterBind) {
                poolCreateInfo.flags(VK14.VK_DESCRIPTOR_POOL_CREATE_UPDATE_AFTER_BIND_BIT);
            }

            LongBuffer pPool = stack.mallocLong(1);
            if (VK14.vkCreateDescriptorPool(ctx.device().vkLogicalDevice(), poolCreateInfo, null, pPool) != VK14.VK_SUCCESS) {
                ctx.throwException(new IllegalStateException("Failed to create descriptor pool!"), "Descriptor Pool");
            }

            this.handle = pPool.get(0);
        }
    }

    public long getHandle() {
        return handle;
    }

    public void reset() {
        VK14.vkResetDescriptorPool(ctx.device().vkLogicalDevice(), handle, 0);
    }

    @Override
    public void free() {
        if (!free) {
            VK14.vkDestroyDescriptorPool(ctx.device().vkLogicalDevice(), handle, null);
            free = true;
        }
    }

}
