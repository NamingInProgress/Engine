package com.vke.core.vulkan.pipeline;

import com.vke.api.pipeline.PipelineData;
import com.vke.api.pipeline.VertexLayoutData;
import com.vke.api.rendering.abstraction.enums.texture.TextureFormat;
import com.vke.api.rendering.abstraction.pipeline.GraphicsPipeline;
import com.vke.api.rendering.abstraction.pipeline.PipelineLayout;
import com.vke.api.rendering.vulkan.pipeline.RenderPipeline;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.utils.tuple.Pair;
import com.vke.utils.tuple.Tripple;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class VulkanRenderPipeline implements GraphicsPipeline {

    private final VKEngine engine;
    private final VulkanRenderDevice device;

    public VulkanRenderPipeline(VKEngine engine, VulkanRenderDevice device, PipelineData data) {
        this.engine = engine;
        this.device = device;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPipelineDynamicStateCreateInfo dynamicStates = getDynamicStates(stack, data.dynamicStates.stream().mapToInt(RenderPipeline.DynamicState::getVkHandle).toArray());

        }
    }

    //region Pipeline Setup
    private VkPipelineDynamicStateCreateInfo getDynamicStates(MemoryStack stack, int[] states) {
        return VkPipelineDynamicStateCreateInfo.calloc(stack)
                .sType$Default()
                .pDynamicStates(stack.ints(states));
    }

    // TODO: this
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

    private Tripple<IntBuffer, VkPipelineColorBlendStateCreateInfo, VkPipelineColorBlendAttachmentState.Buffer> getColorAttachments(MemoryStack stack, PipelineData data) {
        List<PipelineData.ColorAttachmentInfo> colorAttachments = new ArrayList<>();
        for (var attachment : data.attachments) {
            if (attachment instanceof PipelineData.ColorAttachmentInfo colorAttachment) {
                colorAttachments.add(colorAttachment);
            }
        }
        VkPipelineColorBlendAttachmentState.Buffer attachments = VkPipelineColorBlendAttachmentState.calloc(colorAttachments.size(), stack);
        IntBuffer attachmentFormats = stack.mallocInt(colorAttachments.size());

        for (int i = 0; i < colorAttachments.size(); i++) {
            var colorAttachment = colorAttachments.get(i);
            VkPipelineColorBlendAttachmentState currentAttachment = attachments.get(i);
            currentAttachment.srcAlphaBlendFactor(colorAttachment.srcAlphaBlendFactor.getVkHandle());
            currentAttachment.dstAlphaBlendFactor(colorAttachment.dstAlphaBlendFactor.getVkHandle());
            currentAttachment.alphaBlendOp(colorAttachment.alphaBlendOperation.getVkHandle());
            currentAttachment.srcColorBlendFactor(colorAttachment.srcBlendFactor.getVkHandle());
            currentAttachment.dstColorBlendFactor(colorAttachment.dstBlendFactor.getVkHandle());
            currentAttachment.colorBlendOp(colorAttachment.colorBlendOperation.getVkHandle());
            currentAttachment.blendEnable(colorAttachment.blendEnable);
            currentAttachment.colorWriteMask(colorAttachment.colorWriteMask);

            attachmentFormats.put(colorAttachment.format.getVkHandle());
        }

        attachmentFormats.flip();

        VkPipelineColorBlendStateCreateInfo blendStates = VkPipelineColorBlendStateCreateInfo.calloc(stack)
                .sType$Default()
                .attachmentCount(colorAttachments.size())
                .pAttachments(attachments)
                .logicOpEnable(false)
                .logicOp(VK14.VK_LOGIC_OP_COPY);

        for (int i = 0; i < data.blendConstants.length; i++) {
            blendStates.blendConstants(i, data.blendConstants[i]);
        }

        return new Tripple<>(attachmentFormats, blendStates, attachments);
    }

    public VkPipelineRenderingCreateInfo getRenderingInfo(MemoryStack stack, PipelineData data, IntBuffer attachmentFormats) {
        VkPipelineRenderingCreateInfo renderingCreateInfo = VkPipelineRenderingCreateInfo.calloc(stack)
                .sType$Default()
                .pColorAttachmentFormats(attachmentFormats)
                .colorAttachmentCount(attachmentFormats.capacity())
                .depthAttachmentFormat(data.dep)
    }
    //endregion

    @Override
    public PipelineLayout layout() {
        return null;
    }

    @Override
    public void free() {

    }

}
