package com.vke.core.vulkan.pipeline;

import com.vke.api.rendering.abstraction.pipeline.ComputePipeline;
import com.vke.api.rendering.abstraction.pipeline.PipelineLayout;
import com.vke.api.rendering.vulkan.descriptors.DescriptorSets;
import com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.api.rendering.vulkan.pipeline.ComputePipelineData;
import com.vke.api.rendering.vulkan.pipeline.IVulkanPipeline;
import com.vke.api.rendering.vulkan.pushconstants.PushConstantHandle;
import com.vke.api.rendering.vulkan.pushconstants.PushConstants;
import com.vke.core.Context;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.shader.VKShaderProgram;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkComputePipelineCreateInfo;

import java.nio.LongBuffer;
import java.util.List;

public class VulkanComputePipeline implements ComputePipeline, IVulkanPipeline {

    private final Context context;
    private final VulkanRenderDevice device;

    private final VulkanPipelineLayout layout;

    private final long handle;

    public VulkanComputePipeline(Context context, VulkanRenderDevice device, ComputePipelineData data) {
        this.context = context;
        this.device = device;

        data.compiledShaders = VKShaderProgram.asVkShaderProgram(context, data.shader);

        var shaders = getReflectedShaders(context, data.compiledShaders);
        List<DescriptorSetLayout> ds = createDescriptorSets(context, device, shaders);
        PushConstants pc = createPushConstants(shaders);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            var shaderStages = getShaderStages(stack, data.shader, data.compiledShaders);
            this.layout = VulkanPipelineLayout.getLayout(context.getEngine(), device, pc, ds);

            VkComputePipelineCreateInfo.Buffer pipelineCreateInfo = VkComputePipelineCreateInfo.calloc(1, stack);
            pipelineCreateInfo.get(0)
                    .sType$Default()
                    .stage(shaderStages.get(0))
                    .layout(this.layout.getHandle());

            LongBuffer pPipeline = stack.mallocLong(1);
            if (VK14.vkCreateComputePipelines(device.getLogicalDevice().getDevice(),
                    VK14.VK_NULL_HANDLE, pipelineCreateInfo, null, pPipeline) != VK14.VK_SUCCESS) {
                context.throwException(new RuntimeException("Failed to create compute pipeline!"), "ComputePipeline@VulkanImpl");
            }

            this.handle = pPipeline.get(0);
        }
    }

    public PushConstantHandle resolvePushConstant(String path) {
        return this.layout.pushConstants().resolve(path);
    }

    @Override
    public PipelineLayout layout() {
        return this.layout;
    }

    @Override
    public void free() {
        VK14.vkDestroyPipeline(device.getLogicalDevice().getDevice(), handle, null);
        layout.free();
    }

    @Override
    public long getHandle() {
        return this.handle;
    }

}
