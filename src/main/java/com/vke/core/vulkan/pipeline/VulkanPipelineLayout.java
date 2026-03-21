package com.vke.core.vulkan.pipeline;

import com.vke.api.rendering.abstraction.pipeline.PipelineLayout;
import com.vke.api.rendering.vulkan.descriptors.DescriptorSets;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class VulkanPipelineLayout implements PipelineLayout {

    public static final HashMap<LayoutCapabilities, VulkanPipelineLayout> LAYOUT_CACHE = new HashMap<>();

    //private final long handle;

    private final VulkanRenderDevice device;
    private final VKEngine engine;

    private final VulkanPushConstants pushConstants;
    private final DescriptorSets descriptorSets;

    public VulkanPipelineLayout(VKEngine engine, VulkanRenderDevice device, VulkanPushConstants pc, DescriptorSets ds) {
        this.engine = engine;
        this.device = device;
        this.pushConstants = pc;
        this.descriptorSets = ds;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPipelineLayoutCreateInfo createInfo = VkPipelineLayoutCreateInfo.calloc(stack)
                    .sType$Default();

            VkPushConstantRange.Buffer pushConstantsBuffer = VkPushConstantRange.calloc(pc.getPushConstants().size(), stack);

            AtomicInteger pushConstantsCounter = new AtomicInteger(0);
            pc.getPushConstants().forEach((k, v) -> {
                pushConstantsBuffer.get(pushConstantsCounter.getAndIncrement())
                        ;//.offset(v.o)
            });
        }
    }

    @Override
    public int pushConstantSize() {
        return 0;
    }

    @Override
    public int descriptorCount() {
        return 0;
    }

    @Override
    public void free() {

    }

    public record LayoutCapabilities() {}

}
