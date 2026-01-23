package com.vke.core.rendering.vulkan.pipeline;

import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;

import java.nio.LongBuffer;

public class PipelineLayout {

    private long handle;

    public PipelineLayout(VKEngine engine, LogicalDevice device) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPipelineLayoutCreateInfo createInfo = VkPipelineLayoutCreateInfo.calloc(stack)
                    .sType$Default()
                    .setLayoutCount(0)
                    .pPushConstantRanges(null);

            LongBuffer pPipelineLayout = stack.mallocLong(1);
            if (VK14.vkCreatePipelineLayout(device.getDevice(), createInfo, null, pPipelineLayout) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create PipelineLayout"), "PIPELINE_LAYOUT_INIT");
            }
            this.handle = pPipelineLayout.get(0);
        }
    }

    public long getHandle() { return this.handle; }

}
