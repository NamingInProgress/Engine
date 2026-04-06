package com.vke.core.vulkan.pipeline;

import com.vke.api.pipeline.PipelineData;
import com.vke.api.pipeline.VertexLayoutData;
import com.vke.api.rendering.abstraction.pipeline.GraphicsPipeline;
import com.vke.api.rendering.abstraction.pipeline.PipelineLayout;
import com.vke.api.rendering.vulkan.descriptors.DescriptorSets;
import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.api.rendering.vulkan.pipeline.RenderPipeline;
import com.vke.core.Context;
import com.vke.core.VKEngine;
import com.vke.core.services.Services;
import com.vke.core.services.shr.ReflectedShader;
import com.vke.core.services.shr.ShaderReflector;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.shader.VKShaderProgram;
import com.vke.utils.Utils;
import com.vke.utils.io.Identifier;
import com.vke.utils.iter.helpers.Option;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class VulkanRenderPipeline implements GraphicsPipeline {

    private final Context context;
    private final VulkanRenderDevice device;

    private final VulkanPipelineLayout layout;

    private final long handle;

    public VulkanRenderPipeline(Context context, VulkanRenderDevice device, PipelineData data) {
        this.context = context;
        this.device = device;

        fillShaderData(data);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VKEngine engine = context.getEngine();
            var dynamicStates = getDynamicStates(stack, data.dynamicStates.stream().mapToInt(RenderPipeline.DynamicState::getVkHandle).toArray());
            var vertexInputs = getVertexInputs(stack, data.vertexLayoutData);
            var rasterInfo = getRasterInfo(stack, data);
            var inputAssemblyInfo = getInputAssemblyInfo(stack, data);
            var multisampleInfo = getMultisampleCreateInfo(stack, data);
            var colorAttachments = getColorAttachments(stack, data);
            var colorBlendState = getColorBlendState(stack, data, colorAttachments);
            var depthStencilState = getDepthStencilState(stack, data);
            var colorAttachmentFormats = getColorAttachmentFormats(stack, data);
            var renderInfo = getRenderingInfo(stack, data, colorAttachmentFormats);
            var shaderStages = getShaderStages(stack, data);
            var viewportInfo = getViewportInfo(stack);
            this.layout = getPipelineLayout(stack, data);

            VkGraphicsPipelineCreateInfo.Buffer pipelineCreateInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack)
                    .get(0)
                    .sType$Default()
                    .pNext(renderInfo)
                    .pStages(shaderStages)
                    .stageCount(shaderStages.capacity())
                    .pDynamicState(dynamicStates)
                    .pVertexInputState(vertexInputs)
                    .pInputAssemblyState(inputAssemblyInfo)
                    .pRasterizationState(rasterInfo)
                    .pMultisampleState(multisampleInfo)
                    .pColorBlendState(colorBlendState)
                    .pDepthStencilState(depthStencilState)
                    .layout(layout.getHandle())
                    .pViewportState(viewportInfo)
                    .renderPass(VK14.VK_NULL_HANDLE);

            LongBuffer pHandle = stack.mallocLong(1);

            if (VK14.vkCreateGraphicsPipelines(device.getLogicalDevice().getDevice(),
                    VK14.VK_NULL_HANDLE, pipelineCreateInfo, null, pHandle) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Couldn't create graphics pipeline"), "GraphicsPipeline@VulkanImpl");
            }

            this.handle = pHandle.get(0);
        }
    }

    private void fillShaderData(PipelineData data) {
        Identifier[] shaders = data.shaders.getIdentifiers();
        ShaderReflector refl = context.service(Services.SHADER_REFLECTION);
        ArrayList<ReflectedShader> reflectedShaders = new ArrayList<>();

        for (Identifier shader : shaders) {
            Option<ReflectedShader> shaderOpt = refl.get(shader);
            if (shaderOpt.isNone()) throw new IllegalStateException("Requested reflected shader but none was found! This error should not happen");
            reflectedShaders.add(shaderOpt.unwrap());
        }

        fillDescriptors(data, reflectedShaders);
    }

    private void fillDescriptors(PipelineData data, ArrayList<ReflectedShader> shaders) {
        ArrayList<DescriptorSetLayout> layouts = new ArrayList<>();

        for (ReflectedShader shader : shaders) {
            var ubos = shader.getUBOs();

            HashMap<Integer, DescriptorSetLayout> descriptors = new HashMap<>();
            for (ReflectedShader.BufferDescriptorResource ubo : ubos) {
                if (!descriptors.containsKey(ubo.set)) descriptors.put(ubo.set, new DescriptorSetLayout());
            }

            for (ReflectedShader.BufferDescriptorResource ubo : ubos) {
                descriptors.get(ubo.set).bindings.add(new BindingLayout());
            }
        }

        DescriptorSets descriptors = new DescriptorSets(context.getEngine(), device, layouts, data.additionalDescriptorInfo);
    }

    //region Pipeline Setup
    private VkPipelineDynamicStateCreateInfo getDynamicStates(MemoryStack stack, int[] states) {
        return VkPipelineDynamicStateCreateInfo.calloc(stack)
                .sType$Default()
                .pDynamicStates(stack.ints(states));
    }

    private VkPipelineVertexInputStateCreateInfo getVertexInputs(MemoryStack stack, VertexLayoutData data) {
        int offset = 0;
        int amt = data.getAttributeTypes().size();
        VkVertexInputAttributeDescription.Buffer viadb = VkVertexInputAttributeDescription.calloc(amt, stack);
        int i = 0;
        for (VertexLayoutData.Attribute attr : data.getAttributeTypes()) {
            viadb.get(i)
                    .location(i)
                    .binding(0)
                    .offset(offset)
                    .format(attr.getFormat().getVkHandle())
            ; //This semicolon wants to have its own line
            offset += attr.getByteStride();
            i++;
        }

        VkVertexInputBindingDescription.Buffer bindingDesc = VkVertexInputBindingDescription.calloc(1, stack);
        bindingDesc.get(0)
                .binding(0)
                .stride(offset)
                .inputRate(VK14.VK_VERTEX_INPUT_RATE_VERTEX);

        return VkPipelineVertexInputStateCreateInfo.calloc(stack)
                .pVertexAttributeDescriptions(viadb)
                .pVertexBindingDescriptions(bindingDesc)
                .sType$Default();
    }

    private VkPipelineRasterizationStateCreateInfo getRasterInfo(MemoryStack stack, PipelineData data) {
        return VkPipelineRasterizationStateCreateInfo.calloc(stack)
                .sType$Default()
                .polygonMode(data.polygonMode.getVkHandle())
                .cullMode(data.cullMode.getVkHandle())
                .frontFace(data.windingOrder.getVkHandle())
                .lineWidth(data.lineWidth)
                .depthBiasEnable(data.depthBiasEnable)
                .depthBiasClamp(data.depthBiasClamp)
                .depthBiasSlopeFactor(data.depthBiasSlopeFactor)
                .depthClampEnable(false)
                .rasterizerDiscardEnable(false);
    }

    private VkPipelineInputAssemblyStateCreateInfo getInputAssemblyInfo(MemoryStack stack, PipelineData data) {
        return VkPipelineInputAssemblyStateCreateInfo.calloc(stack)
                .sType$Default()
                .topology(data.topology.getVkHandle())
                .primitiveRestartEnable(data.primitiveRestartEnable);
    }

    private VkPipelineMultisampleStateCreateInfo getMultisampleCreateInfo(MemoryStack stack, PipelineData data) {
        return VkPipelineMultisampleStateCreateInfo.calloc(stack)
                .sType$Default()
                .rasterizationSamples(VK14.VK_SAMPLE_COUNT_1_BIT)
                .sampleShadingEnable(false);
    }

    private VkPipelineColorBlendAttachmentState.Buffer getColorAttachments(MemoryStack stack, PipelineData data) {
        VkPipelineColorBlendAttachmentState.Buffer attachments =
                VkPipelineColorBlendAttachmentState.calloc(data.colorAttachments.size(), stack);

        for (int i = 0; i < data.colorAttachments.size(); i++) {
            PipelineData.ColorAttachmentInfo colorAttachment = data.colorAttachments.get(i);

            attachments.get(i)
                .srcAlphaBlendFactor(colorAttachment.srcAlphaBlendFactor.getVkHandle())
                .dstAlphaBlendFactor(colorAttachment.dstAlphaBlendFactor.getVkHandle())
                .alphaBlendOp(colorAttachment.alphaBlendOperation.getVkHandle())
                .srcColorBlendFactor(colorAttachment.srcBlendFactor.getVkHandle())
                .dstColorBlendFactor(colorAttachment.dstBlendFactor.getVkHandle())
                .colorBlendOp(colorAttachment.colorBlendOperation.getVkHandle())
                .blendEnable(colorAttachment.blendEnable)
                .colorWriteMask(colorAttachment.colorWriteMask);
        }

        return attachments;
    }

    private VkPipelineColorBlendStateCreateInfo getColorBlendState(MemoryStack stack, PipelineData data,
                                                                   VkPipelineColorBlendAttachmentState.Buffer attachments) {
        VkPipelineColorBlendStateCreateInfo blendStates = VkPipelineColorBlendStateCreateInfo.calloc(stack)
                .sType$Default()
                .attachmentCount(data.colorAttachments.size())
                .pAttachments(attachments)
                .logicOpEnable(false)
                .logicOp(VK14.VK_LOGIC_OP_COPY);

        for (int i = 0; i < data.blendConstants.length; i++) {
            blendStates.blendConstants(i, data.blendConstants[i]);
        }

        return blendStates;
    }

    private VkPipelineDepthStencilStateCreateInfo getDepthStencilState(MemoryStack stack, PipelineData data) {
        VkPipelineDepthStencilStateCreateInfo info = VkPipelineDepthStencilStateCreateInfo.calloc(stack)
                .sType$Default()
                .depthBoundsTestEnable(false);

        if (data.depthAttachment != null) {
            info.depthTestEnable(data.depthAttachment.depthTestEnable);
            info.depthWriteEnable(data.depthAttachment.depthWriteEnable);
            info.depthCompareOp(data.depthAttachment.depthCompareOp.getVkHandle());
        }

        if (data.stencilAttachment != null) {
            info.stencilTestEnable(data.stencilAttachment.stencilTestEnable);
            info.front(data.stencilAttachment.frontStencilOp.asVkObject(stack));
            info.back(data.stencilAttachment.backStencilOp.asVkObject(stack));
        }

        return info;
    }

    private IntBuffer getColorAttachmentFormats(MemoryStack stack, PipelineData data) {
        IntBuffer attachmentFormats = stack.mallocInt(data.colorAttachments.size());
        data.colorAttachments.forEach(att -> attachmentFormats.put(att.format.getVkHandle()));
        attachmentFormats.flip();
        return attachmentFormats;
    }

    public VkPipelineRenderingCreateInfo getRenderingInfo(MemoryStack stack, PipelineData data, IntBuffer attachmentFormats) {
        return VkPipelineRenderingCreateInfo.calloc(stack)
                .sType$Default()
                .colorAttachmentCount(data.colorAttachments.size())
                .pColorAttachmentFormats(attachmentFormats)
                .depthAttachmentFormat(data.depthAttachment.format.getVkHandle())
                .stencilAttachmentFormat(data.stencilAttachment.format.getVkHandle());
    }

    public VkPipelineShaderStageCreateInfo.Buffer getShaderStages(MemoryStack stack, PipelineData data) {
        VkPipelineShaderStageCreateInfo.Buffer stages =
                VkPipelineShaderStageCreateInfo.calloc(data.shaders.getShaderCount(), stack);

        VKShaderProgram sp = VKShaderProgram.asVkShaderProgram(context, data.shaders);

        VkPipelineShaderStageCreateInfo[] shaderStageCreateInfos = sp.getShaderCreateInfos();

        for (int i = 0; i < shaderStageCreateInfos.length; i++) {
            VkPipelineShaderStageCreateInfo stage = shaderStageCreateInfos[i];

            stages.get(i).sType$Default()
                    .stage(stage.stage())
                    .module(stage.module())
                    .pName(Utils.ensureCStr(stage.pName()));
        }
        return stages;
    }

    public VkPipelineViewportStateCreateInfo getViewportInfo(MemoryStack stack) {
        return VkPipelineViewportStateCreateInfo.calloc(stack)
                .sType$Default()
                .viewportCount(1)
                .scissorCount(1);
    }

    public VulkanPipelineLayout getPipelineLayout(VKEngine engine, VulkanRenderDevice device, MemoryStack stack, PipelineData data) {
        // TODO: FIX THIS
        return VulkanPipelineLayout.getLayout(engine, device, null, null);
    }
    //endregion

    @Override
    public PipelineLayout layout() {
        return layout;
    }

    @Override
    public void free() {
        VK14.vkDestroyPipeline(device.getLogicalDevice().getDevice(), handle, null);
        layout.free();
    }

}
