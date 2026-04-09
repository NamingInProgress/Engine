package com.vke.core.vulkan.pipeline;

import com.vke.api.rendering.abstraction.pipeline.PipelineLayout;
import com.vke.api.rendering.vulkan.descriptors.DescriptorSets;
import com.vke.core.vulkan.descriptor.CompiledDescriptorSetLayout;
import com.vke.api.rendering.vulkan.pushconstants.PushConstants;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.utils.Utils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;

import java.nio.LongBuffer;
import java.util.HashMap;

public class VulkanPipelineLayout implements PipelineLayout {

    public static final HashMap<LayoutCapabilities, VulkanPipelineLayout> LAYOUT_CACHE = new HashMap<>();

    private final long handle;

    private final VulkanRenderDevice device;
    private final VKEngine engine;

    private final PushConstants pushConstants;
    private final DescriptorSets descriptorSets;

    public static VulkanPipelineLayout getLayout(VKEngine engine, VulkanRenderDevice device, PushConstants pc, DescriptorSets ds) {
        // TODO: Fix this making a new pipeline layout (This is technically fine but it is recommended to reuse)
        if (Utils.TRUE) return new VulkanPipelineLayout(engine, device, pc, ds);

        LayoutCapabilities cap = new LayoutCapabilities();
        if (LAYOUT_CACHE.containsKey(cap)) return LAYOUT_CACHE.get(cap);
        LAYOUT_CACHE.put(cap, new VulkanPipelineLayout(engine, device, pc, ds));
        return LAYOUT_CACHE.get(cap);
    }

    private VulkanPipelineLayout(VKEngine engine, VulkanRenderDevice device, PushConstants pc, DescriptorSets ds) {
        this.engine = engine;
        this.device = device;
        this.pushConstants = pc;
        this.descriptorSets = ds;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPipelineLayoutCreateInfo createInfo = VkPipelineLayoutCreateInfo.calloc(stack)
                    .sType$Default();

            VkPushConstantRange.Buffer pushConstantsBuffer = VkPushConstantRange.calloc(1, stack);
            pushConstantsBuffer.get(0)
                    .offset(0)
                    .size((int) pc.getLayout().size)
                    .stageFlags(VK14.VK_SHADER_STAGE_ALL);

            LongBuffer pDescriptors = stack.longs(ds.getCompiledLayouts().stream().mapToLong(CompiledDescriptorSetLayout::getHandle).toArray());

            createInfo.pSetLayouts(pDescriptors);
            createInfo.setLayoutCount(ds.getCompiledLayouts().size());
            createInfo.pPushConstantRanges(pushConstantsBuffer);

            LongBuffer pLayout = stack.mallocLong(1);
            if (VK14.vkCreatePipelineLayout(device.getLogicalDevice().getDevice(), createInfo, null, pLayout) != VK14.VK_SUCCESS) {
                engine.throwException(new RuntimeException("Failed to create Pipeline Layout"), "PipelineLayout@VulkanImpl");
            }

            this.handle = pLayout.get(0);
            this.pushConstants.setHandle(this.handle);
        }
    }

    @Override
    public int pushConstantSize() {
        return (int) pushConstants.getLayout().size;
    }

    @Override
    public int descriptorCount() {
        return descriptorSets.getCompiledLayouts().size();
    }

    public long getHandle() { return this.handle; }

    public DescriptorSets descriptors() {
        return descriptorSets;
    }

    public PushConstants pushConstants() {
        return pushConstants;
    }

    @Override
    public void free() {
        // destroy descriptors and stuff
        VK14.vkDestroyPipelineLayout(device.getLogicalDevice().getDevice(), this.handle, null);
        descriptorSets.free();
        pushConstants.free();
    }

    public record LayoutCapabilities() {}

}
