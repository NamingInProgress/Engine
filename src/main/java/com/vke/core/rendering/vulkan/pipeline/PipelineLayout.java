package com.vke.core.rendering.vulkan.pipeline;

import com.vke.api.vulkan.pipeline.PushConstantsDefinition;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.descriptor.DescriptorSetLayout;
import com.vke.core.rendering.vulkan.descriptor.ref.DescriptorSet;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.utils.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;

import java.nio.LongBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PipelineLayout implements Disposable {

    private long handle;
    private final LinkedHashMap<String, PushConstantsDefinition> pushConstants;
    private final LogicalDevice device;

    public PipelineLayout(VKEngine engine, LogicalDevice device, LinkedHashMap<String, PushConstantsDefinition> pushConstants, List<DescriptorSetLayout> descriptors) {
        this.pushConstants = pushConstants;
        this.device = device;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPipelineLayoutCreateInfo createInfo = VkPipelineLayoutCreateInfo.calloc(stack)
                    .sType$Default()
                    .setLayoutCount(0);

            VkPushConstantRange.Buffer pushConstantsBuffer = VkPushConstantRange.calloc(pushConstants.size(), stack);

            AtomicInteger pcCounter = new AtomicInteger(0);
            pushConstants.forEach((k, v) -> {
                int i = pcCounter.getAndIncrement();

                pushConstantsBuffer.get(i)
                        .offset(v.getOffset())
                        .size(v.getSize(PushConstantsDefinition.ALIGN))
                        .stageFlags(v.getAplicableStages().getVkHandle());
            });

            LongBuffer pDescriptors = stack.longs(descriptors.stream().mapToLong(DescriptorSetLayout::getHandle).toArray());
            createInfo.pSetLayouts(pDescriptors);
            createInfo.setLayoutCount(descriptors.size());

            createInfo.pPushConstantRanges(pushConstantsBuffer);

            LongBuffer pPipelineLayout = stack.mallocLong(1);
            if (VK14.vkCreatePipelineLayout(device.getDevice(), createInfo, null, pPipelineLayout) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create PipelineLayout"), "PIPELINE_LAYOUT_INIT");
            }
            this.handle = pPipelineLayout.get(0);
        }
    }

    public long getHandle() { return this.handle; }

    public LinkedHashMap<String, PushConstantsDefinition>  getPushConstants() {
        return pushConstants;
    }

    public PushConstantsDefinition getPushConst(String key) {
        return this.pushConstants.get(key);
    }

    @Override
    public void free() {
        VK14.vkDestroyPipelineLayout(device.getDevice(), handle, null);
    }
}
