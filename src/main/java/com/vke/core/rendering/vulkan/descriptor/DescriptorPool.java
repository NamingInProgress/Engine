package com.vke.core.rendering.vulkan.descriptor;

import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.utils.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;

import java.nio.LongBuffer;

public class DescriptorPool implements Disposable {
    private long vkHandle;
    private LogicalDevice device;

    public DescriptorPool(DescriptorPoolCreateInfo createInfo) {
        this.device = createInfo.logicalDevice;

        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorPoolSize.Buffer buf = VkDescriptorPoolSize.calloc(createInfo.ratios.size(), stack);

            int i = 0;
            for (DescriptorPool.Ratio ratio : createInfo.ratios) {
                buf.get(i++)
                        .type(ratio.type().getVkHandle())
                        .descriptorCount((int) ratio.ratio() * createInfo.maxSets);
            }

            VkDescriptorPoolCreateInfo ci = VkDescriptorPoolCreateInfo.calloc(stack);
            ci.sType$Default();
            ci.flags(0);
            ci.maxSets(createInfo.maxSets);
            ci.pPoolSizes(buf);

            LongBuffer pPool = stack.mallocLong(1);
            if (VK14.vkCreateDescriptorPool(device.getDevice(), ci, null, pPool) != VK14.VK_SUCCESS) {
                createInfo.engine.throwException(new IllegalStateException("Failed to create descriptor pool!"), "Descriptor Pool");
            }
            this.vkHandle = pPool.get(0);
        }
    }

    @Override
    public void free() {
        VK14.vkDestroyDescriptorPool(device.getDevice(), vkHandle, null);
    }

    public long getHandle() { return this.vkHandle; }

    public record Ratio(float ratio, DescriptorType type) {
    }
}
