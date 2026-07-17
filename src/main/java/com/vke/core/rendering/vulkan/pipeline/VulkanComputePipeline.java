package com.vke.core.rendering.vulkan.pipeline;

import com.vke.api.rendering.abstraction.renderer.pipeline.ComputePipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.PipelineLayout;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.ShaderResource;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.api.rendering.vulkan.pipeline.ComputePipelineData;
import com.vke.api.rendering.vulkan.pipeline.IVulkanPipeline;
import com.vke.api.rendering.vulkan.pushconstants.PushConstantHandle;
import com.vke.api.rendering.vulkan.pushconstants.PushConstants;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import com.vke.core.rendering.vulkan.shader.VKShaderProgram;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkComputePipelineCreateInfo;

import java.nio.LongBuffer;
import java.util.List;

public class VulkanComputePipeline implements ComputePipeline, IVulkanPipeline {

    private final VulkanRenderSystem ctx;

    private final VulkanPipelineLayout layout;

    private final long handle;

    public VulkanComputePipeline(VulkanRenderSystem ctx, ComputePipelineData data) {
        this.ctx = ctx;

        data.compiledShaders = VKShaderProgram.asVkShaderProgram(ctx, data.shader);

        var shaders = getReflectedShaders(ctx, data.compiledShaders);
        List<DescriptorSetLayout> ds = createDescriptorSets(ctx, shaders);
        PushConstants pc = createPushConstants(shaders);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            var shaderStages = getShaderStages(stack, data.shader, data.compiledShaders);
            this.layout = VulkanPipelineLayout.getLayout(ctx, pc, ds);

            VkComputePipelineCreateInfo.Buffer pipelineCreateInfo = VkComputePipelineCreateInfo.calloc(1, stack);
            pipelineCreateInfo.get(0)
                    .sType$Default()
                    .stage(shaderStages.get(0))
                    .layout(this.layout.getHandle());

            LongBuffer pPipeline = stack.mallocLong(1);
            if (VK14.vkCreateComputePipelines(ctx.device().vkLogicalDevice(),
                    VK14.VK_NULL_HANDLE, pipelineCreateInfo, null, pPipeline) != VK14.VK_SUCCESS) {
                ctx.throwException(new RuntimeException("Failed to create compute pipeline!"), "ComputePipeline@VulkanImpl");
            }

            this.handle = pPipeline.get(0);
        }
    }

    @Override
    public PipelineLayout layout() {
        return this.layout;
    }

    @Override
    public <T extends ShaderResource> T resource(String name) {
        return layout.resource(name);
    }

    @Override
    public void free() {
        VK14.vkDestroyPipeline(ctx.device().vkLogicalDevice(), handle, null);
        layout.free();
    }

    @Override
    public long getHandle() {
        return this.handle;
    }

}
