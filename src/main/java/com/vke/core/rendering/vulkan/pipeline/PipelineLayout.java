package com.vke.core.rendering.vulkan.pipeline;

import com.vke.api.vulkan.pipeline.PushConstantsDefinition;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;

import java.nio.LongBuffer;

public class PipelineLayout {

    private long handle;
    private final PushConstantsDefinition[] pushConstants;

    public PipelineLayout(VKEngine engine, LogicalDevice device, PushConstantsDefinition... pushConstants) {
        this.pushConstants = pushConstants;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPipelineLayoutCreateInfo createInfo = VkPipelineLayoutCreateInfo.calloc(stack)
                    .sType$Default()
                    .setLayoutCount(0);

            VkPushConstantRange.Buffer pushConstantsBuffer = VkPushConstantRange.calloc(pushConstants.length, stack);
            for (int i = 0; i < pushConstants.length; i++) {
                PushConstantsDefinition pc = pushConstants[i];

                pushConstantsBuffer.get(i)
                        .offset(pc.getOffset())
                        .size(pc.getSize())
                        .stageFlags(pc.getAplicableStages().getVkHandle());
            }

            createInfo.pPushConstantRanges(pushConstantsBuffer);

            LongBuffer pPipelineLayout = stack.mallocLong(1);
            if (VK14.vkCreatePipelineLayout(device.getDevice(), createInfo, null, pPipelineLayout) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create PipelineLayout"), "PIPELINE_LAYOUT_INIT");
            }
            this.handle = pPipelineLayout.get(0);
        }
    }

    public long getHandle() { return this.handle; }

    public PushConstantsDefinition[] getPushConstants() {
        return pushConstants;
    }
}
