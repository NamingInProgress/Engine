package com.vke.core.vulkan.pipeline;

import com.vke.api.pipeline.DescriptorData;
import com.vke.api.vulkan.pipeline.PushConstantsDefinition;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.descriptor.DescriptorSetLayout;
import com.vke.core.vulkan.device.LogicalDevice;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;

import java.nio.LongBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PipelineLayout implements com.vke.api.abstraction.pipeline.PipelineLayout {

    private long handle;
    private final LinkedHashMap<String, PushConstantsDefinition> pushConstants;
    private final LogicalDevice device;
    private final DescriptorData descriptorData;

    public PipelineLayout(VKEngine engine, LogicalDevice device, LinkedHashMap<String, PushConstantsDefinition> pushConstants, DescriptorData data, List<DescriptorSetLayout> descriptors) {
        this.pushConstants = pushConstants;
        this.device = device;
        this.descriptorData = data;
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

    @Override
    public DescriptorData getDescriptors() {
        return descriptorData;
    }

    @Override
    public int pushConstantSize() {
        return this.pushConstants.size();
    }

    @Override
    public int descriptorCount() {
        return descriptorData.getSetsAmount();
    }
}
