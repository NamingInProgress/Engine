package com.vke.core.vulkan.pipeline;

import com.vke.api.rendering.abstraction.enums.ShaderType;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.api.rendering.vulkan.pipeline.IVulkanPipeline;
import com.vke.api.rendering.vulkan.pipeline.RenderPipelineData;
import com.vke.api.rendering.vulkan.pipeline.VertexLayoutData;
import com.vke.api.rendering.abstraction.enums.texture.Format;
import com.vke.api.rendering.abstraction.pipeline.RenderPipeline;
import com.vke.api.rendering.vulkan.pushconstants.PushConstantHandle;
import com.vke.api.rendering.vulkan.pushconstants.PushConstants;
import com.vke.core.Context;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.shr.ReflectedShader;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.shader.VKShaderProgram;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.*;

public class VulkanRenderPipeline implements RenderPipeline, IVulkanPipeline {

    private final Context context;
    private final VulkanRenderDevice device;

    private final VulkanPipelineLayout layout;

    private final long handle;

    public VulkanRenderPipeline(Context context, VulkanRenderDevice device, RenderPipelineData data) {
        this.context = context;
        this.device = device;

        data.compiledShaders = VKShaderProgram.asVkShaderProgram(context, data.shaders);

        var shaders = getReflectedShaders(context, data.compiledShaders);
        List<DescriptorSetLayout> ds = createDescriptorSets(context, shaders);
        PushConstants pc = createPushConstants(shaders);
        data.vertexLayoutData = createVertexLayouts(data, shaders);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VKEngine engine = context.getEngine();
            var dynamicStates = getDynamicStates(stack, data.dynamicStates.stream().mapToInt(RenderPipelineData.DynamicState::getVkHandle).toArray());
            var vertexInputs = getVertexInputs(stack, data.vertexLayoutData);
            var rasterInfo = getRasterInfo(stack, data);
            var inputAssemblyInfo = getInputAssemblyInfo(stack, data);
            var multisampleInfo = getMultisampleCreateInfo(stack, data);
            var colorAttachments = getColorAttachments(stack, data);
            var colorBlendState = getColorBlendState(stack, data, colorAttachments);
            var depthStencilState = getDepthStencilState(stack, data);
            var colorAttachmentFormats = getColorAttachmentFormats(stack, data);
            var renderInfo = getRenderingInfo(stack, data, colorAttachmentFormats);
            var shaderStages = getShaderStages(stack, data.shaders, data.compiledShaders);
            var viewportInfo = getViewportInfo(stack);
            this.layout = VulkanPipelineLayout.getLayout(engine, device, pc, ds);

            VkGraphicsPipelineCreateInfo.Buffer pipelineCreateInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack);
            pipelineCreateInfo.get(0)
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
                context.throwException(new IllegalStateException("Couldn't create graphics pipeline"), "GraphicsPipeline@VulkanImpl");
            }

            this.handle = pHandle.get(0);
        }
    }

    public PushConstantHandle resolvePushConstant(String path) {
        return this.layout.pushConstants().resolve(path);
    }

    private VertexLayoutData createVertexLayouts(RenderPipelineData data, ArrayList<ReflectedShader> shaders) {
        ArrayList<VertexLayoutData.Attribute> attribs = new ArrayList<>();

        for (ReflectedShader shader : shaders) {
            if (shader.getShaderType() == ShaderType.FRAGMENT) continue;
            var vaos = shader.getVAOs().stream().sorted(Comparator.comparingInt(c -> c.location));

            vaos.forEach(resource -> attribs.add(
                    new VertexLayoutData.Attribute(resource.stride, Format.fromBaseType(resource.baseType, resource.vecSize))
            ));
        }

        return new VertexLayoutData(attribs);
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

        if (amt == 0) return VkPipelineVertexInputStateCreateInfo.calloc(stack).sType$Default();

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

    private VkPipelineRasterizationStateCreateInfo getRasterInfo(MemoryStack stack, RenderPipelineData data) {
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

    private VkPipelineInputAssemblyStateCreateInfo getInputAssemblyInfo(MemoryStack stack, RenderPipelineData data) {
        return VkPipelineInputAssemblyStateCreateInfo.calloc(stack)
                .sType$Default()
                .topology(data.topology.getVkHandle())
                .primitiveRestartEnable(data.primitiveRestartEnable);
    }

    private VkPipelineMultisampleStateCreateInfo getMultisampleCreateInfo(MemoryStack stack, RenderPipelineData data) {
        return VkPipelineMultisampleStateCreateInfo.calloc(stack)
                .sType$Default()
                .rasterizationSamples(VK14.VK_SAMPLE_COUNT_1_BIT)
                .sampleShadingEnable(false);
    }

    private VkPipelineColorBlendAttachmentState.Buffer getColorAttachments(MemoryStack stack, RenderPipelineData data) {
        VkPipelineColorBlendAttachmentState.Buffer attachments =
                VkPipelineColorBlendAttachmentState.calloc(data.colorAttachments.size(), stack);

        for (int i = 0; i < data.colorAttachments.size(); i++) {
            RenderPipelineData.ColorAttachmentInfo colorAttachment = data.colorAttachments.get(i);

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

    private VkPipelineColorBlendStateCreateInfo getColorBlendState(MemoryStack stack, RenderPipelineData data,
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

    private VkPipelineDepthStencilStateCreateInfo getDepthStencilState(MemoryStack stack, RenderPipelineData data) {
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

    private IntBuffer getColorAttachmentFormats(MemoryStack stack, RenderPipelineData data) {
        IntBuffer attachmentFormats = stack.mallocInt(data.colorAttachments.size());
        data.colorAttachments.forEach(att -> attachmentFormats.put(att.format.getVkHandle()));
        attachmentFormats.flip();
        return attachmentFormats;
    }

    private VkPipelineRenderingCreateInfo getRenderingInfo(MemoryStack stack, RenderPipelineData data, IntBuffer attachmentFormats) {
        return VkPipelineRenderingCreateInfo.calloc(stack)
                .sType$Default()
                .colorAttachmentCount(data.colorAttachments.size())
                .pColorAttachmentFormats(attachmentFormats)
                .depthAttachmentFormat(data.depthAttachment == null ? 0 : data.depthAttachment.format.getVkHandle())
                .stencilAttachmentFormat(data.stencilAttachment == null ? 0 : data.stencilAttachment.format.getVkHandle());
    }

    private VkPipelineViewportStateCreateInfo getViewportInfo(MemoryStack stack) {
        return VkPipelineViewportStateCreateInfo.calloc(stack)
                .sType$Default()
                .viewportCount(1)
                .scissorCount(1);
    }
    //endregion

    @Override
    public long getHandle() {
        return this.handle;
    }

    @Override
    public VulkanPipelineLayout layout() {
        return layout;
    }

    @Override
    public void free() {
        VK14.vkDestroyPipeline(device.getLogicalDevice().getDevice(), handle, null);
        layout.free();
    }

}
