package com.vke.core.rendering.vulkan.pipeline;

import com.carrotsearch.hppc.ObjectIntHashMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.carrotsearch.hppc.cursors.ObjectIntCursor;
import com.vke.api.vulkan.createInfos.PipelineCreateInfo;
import com.vke.api.vulkan.descriptors.DescriptorData;
import com.vke.api.vulkan.pipeline.PushConstantsDefinition;
import com.vke.api.vulkan.pipeline.RenderPipeline;
import com.vke.core.VKEngine;
import com.vke.core.rendering.buffer.BufferSlice;
import com.vke.core.rendering.vulkan.VKUtils;
import com.vke.core.rendering.vulkan.descriptor.*;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.rendering.vulkan.shader.VKShaderProgram;
import com.vke.core.rendering.vulkan.swapchain.SwapChain;
import com.vke.utils.Disposable;
import com.vke.utils.Utils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

public class GraphicsPipeline implements Disposable {
    private static String HERE = "GraphicsPipeline";

    private long handle;
    private LogicalDevice device;
    private VKEngine engine;
    private SwapChain swapChain;
    private PipelineLayout layout;
    private List<DescriptorSetLayout> descriptorSetLayouts = new ArrayList<>();

    private DescriptorData descriptorData;

    private DescriptorAllocator descAlloc;

    public GraphicsPipeline(PipelineCreateInfo createInfo, PipelineSettingsInfo pipelineSettingsInfo) {
        this.device = createInfo.device;
        this.engine = createInfo.engine;
        this.swapChain = createInfo.swapChain;

        List<DescriptorPool.DescriptorTypeCountInfo> descriptorTypeCountInfo = new ArrayList<>();

        for (ObjectIntCursor<DescriptorType> count : descriptorData.counts()) {
            descriptorTypeCountInfo.add(new DescriptorPool.DescriptorTypeCountInfo(count.value, count.key));
        }

        descriptorData = pipelineSettingsInfo.descriptorData;

        for (IntObjectCursor<DescriptorData.Set> set : descriptorData.getSets()) {
            DescriptorSetLayout.Builder builder = new DescriptorSetLayout.Builder();

            for (IntObjectCursor<DescriptorData.Binding> binding : set.value.getBindings()) {

            }

            descriptorSetLayouts.add(builder.build(engine, device));
        }

        if (descriptorData.getSetsAmount() != 0) {
            DescriptorPoolCreateInfo poolCreateInfo = new DescriptorPoolCreateInfo();
            poolCreateInfo.maxSets = descriptorData.getSetsAmount();
            poolCreateInfo.engine = engine;
            poolCreateInfo.logicalDevice = device;
            poolCreateInfo.descriptorTypeCountInfo = descriptorTypeCountInfo;

            descAlloc = new DescriptorAllocator(poolCreateInfo);


        }

        try(MemoryStack stack = MemoryStack.stackPush()) {
            int colorAttachmentCounts = pipelineSettingsInfo.colorAttachments().size();
            IntBuffer dynamicStates = stack.ints(pipelineSettingsInfo.dynamicStates());

            VkPipelineDynamicStateCreateInfo dynamicStateCreateInfo = VkPipelineDynamicStateCreateInfo.calloc(stack)
                    .sType$Default()
                    .pDynamicStates(dynamicStates);

            VkPipelineVertexInputStateCreateInfo vertexInputStateCreateInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack)
                    .sType$Default();
            //TODO: actually describe inputs later when we use vbo

            VkPipelineRasterizationStateCreateInfo rasterizationStateCreateInfo = VkPipelineRasterizationStateCreateInfo.calloc(stack)
                    .sType$Default()
                    .polygonMode(pipelineSettingsInfo.polygonMode())
                    .cullMode(pipelineSettingsInfo.cullMode())
                    .frontFace(pipelineSettingsInfo.frontFace())
                    .lineWidth(pipelineSettingsInfo.lineWidth())
                    .depthBiasEnable(pipelineSettingsInfo.depthBiasEnable())
                    .depthBiasConstantFactor(pipelineSettingsInfo.depthBiasConstFactor())
                    .depthBiasClamp(pipelineSettingsInfo.depthBiasClamp())
                    .depthBiasSlopeFactor(pipelineSettingsInfo.depthBiasSlopeFactor())
                    .depthClampEnable(false)
                    .rasterizerDiscardEnable(false);

            VkPipelineInputAssemblyStateCreateInfo inputAssemblyStateCreateInfo = VkPipelineInputAssemblyStateCreateInfo.calloc(stack)
                    .sType$Default()
                    .topology(pipelineSettingsInfo.topology())
                    .primitiveRestartEnable(pipelineSettingsInfo.primitiveRestartEnable());

            // Set by default and not changeable for now
            VkPipelineMultisampleStateCreateInfo multisampleStateCreateInfo = VkPipelineMultisampleStateCreateInfo.calloc(stack)
                    .sType$Default()
                    .rasterizationSamples(VK14.VK_SAMPLE_COUNT_1_BIT)
                    .sampleShadingEnable(false);

            VkPipelineColorBlendAttachmentState.Buffer attachments = VkPipelineColorBlendAttachmentState.calloc(
                colorAttachmentCounts, stack);
            IntBuffer attachmentFormats = stack.mallocInt(colorAttachmentCounts);
            for (RenderPipeline.ColorAttachmentInfo colorAttachmentInfo : pipelineSettingsInfo.colorAttachments()) {
                VkPipelineColorBlendAttachmentState attachment = VkPipelineColorBlendAttachmentState.calloc(stack)
                        .colorWriteMask(colorAttachmentInfo.getColorWriteMask())
                        .blendEnable(colorAttachmentInfo.isBlendEnable())
                        .srcColorBlendFactor(colorAttachmentInfo.getSrcBlendFactor().getVkHandle())
                        .dstColorBlendFactor(colorAttachmentInfo.getDstBlendFactor().getVkHandle())
                        .srcAlphaBlendFactor(colorAttachmentInfo.getSrcAlphaBlendFactor().getVkHandle())
                        .dstAlphaBlendFactor(colorAttachmentInfo.getDstAlphaBlendFactor().getVkHandle())
                        .colorBlendOp(colorAttachmentInfo.getColorBlendOperation().getVkHandle())
                        .alphaBlendOp(colorAttachmentInfo.getAlphaBlendOperation().getVkHandle());
                attachments.put(attachment);
                attachmentFormats.put(colorAttachmentInfo.getFormat());
            }

            attachments.flip();
            attachmentFormats.flip();

            VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.calloc(stack)
                    .sType$Default()
                    .attachmentCount(colorAttachmentCounts)
                    .pAttachments(attachments)
                    .logicOpEnable(false)
                    .logicOp(VK14.VK_LOGIC_OP_COPY);

            VkPipelineDepthStencilStateCreateInfo depthStencilInfo = null;
            if (pipelineSettingsInfo.depthStencilAttachment() != null) {
                 depthStencilInfo = VkPipelineDepthStencilStateCreateInfo.calloc(stack)
                        .sType$Default()
                        .depthTestEnable(pipelineSettingsInfo.depthStencilAttachment().isDepthTestEnable())
                        .depthWriteEnable(pipelineSettingsInfo.depthStencilAttachment().isDepthWriteEnable())
                        .depthCompareOp(pipelineSettingsInfo.depthStencilAttachment().getDepthCompareOp().getVkHandle())
                        .stencilTestEnable(pipelineSettingsInfo.depthStencilAttachment().isStencilTestEnable())
                        .front(pipelineSettingsInfo.depthStencilAttachment().getFrontStencilOp().asVkObject(stack))
                        .back(pipelineSettingsInfo.depthStencilAttachment().getBackStencilOp().asVkObject(stack))
                        .depthBoundsTestEnable(false);
            }


            PipelineLayout pipelineLayout = new PipelineLayout(engine, device, pipelineSettingsInfo.pushConstants());
            this.layout = pipelineLayout;

            VkPipelineRenderingCreateInfo renderingCreateInfo = VkPipelineRenderingCreateInfo.calloc(stack)
                    .sType$Default()
                    .pColorAttachmentFormats(attachmentFormats)
                    .colorAttachmentCount(colorAttachmentCounts)
                    .depthAttachmentFormat(pipelineSettingsInfo.depthFormat())
                    .stencilAttachmentFormat(pipelineSettingsInfo.stencilFormat());

            VkPipelineShaderStageCreateInfo[] stagesArr = pipelineSettingsInfo.shader().getShaderCreateInfos();

            VkPipelineShaderStageCreateInfo.Buffer stages = VkPipelineShaderStageCreateInfo.calloc(stagesArr.length, stack);
            for (int i = 0; i < stagesArr.length; i++) {
                VkPipelineShaderStageCreateInfo stage = stagesArr[i];

                stages.get(i).sType$Default()
                        .stage(stage.stage())
                        .module(stage.module())
                        .pName(Utils.ensureCStr(stage.pName()));
            }

            VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.calloc(stack)
                    .sType$Default()
                    .viewportCount(1)
                    .scissorCount(1);

            VkGraphicsPipelineCreateInfo info = VkGraphicsPipelineCreateInfo.calloc(stack)
                    .sType$Default()
                    .pNext(renderingCreateInfo)
                    .pStages(stages)
                    .stageCount(stagesArr.length)
                    .pDynamicState(dynamicStateCreateInfo)
                    .pVertexInputState(vertexInputStateCreateInfo)
                    .pInputAssemblyState(inputAssemblyStateCreateInfo)
                    .pRasterizationState(rasterizationStateCreateInfo)
                    .pMultisampleState(multisampleStateCreateInfo)
                    .pColorBlendState(colorBlending)
                    .pDepthStencilState(depthStencilInfo)
                    .layout(pipelineLayout.getHandle())
                    .pViewportState(viewportState)
                    .renderPass(VK14.VK_NULL_HANDLE);

            VkGraphicsPipelineCreateInfo.Buffer pipelineInfos = VkGraphicsPipelineCreateInfo.calloc(1, stack);
            pipelineInfos.put(info);
            pipelineInfos.flip();

            LongBuffer pPipeline = stack.mallocLong(1);

            if (VK14.vkCreateGraphicsPipelines(device.getDevice(), VK14.VK_NULL_HANDLE, pipelineInfos, null, pPipeline) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Couldn't create graphics pipeline"), HERE);
            }

            this.handle = pPipeline.get(0);
            if (engine.isDebugMode()) {
                if (!VKUtils.setDebugName(device, createInfo.name, this.handle, VK14.VK_OBJECT_TYPE_PIPELINE)) {
                    engine.throwException(new IllegalStateException("Couldn't set debug name"), HERE);
                }
            }
        }
    }

    public long getHandle() { return this.handle; }
    public PipelineLayout getPipelineLayout() { return this.layout; }
    public DescriptorData getDescriptorData() { return this.descriptorData; }

    public void writeToDescriptor(String name, Consumer<BufferSlice> runnable) {

    }

    @Override
    public void free() {
        layout.free();
        descriptorSetLayouts.forEach(DescriptorSetLayout::free);
        if (descAlloc != null) descAlloc.free();
        VK14.vkDestroyPipeline(device.getDevice(), handle, null);
    }

    public record PipelineSettingsInfo(
            // Dynamic States
            int[] dynamicStates,

            // Input Assembly
            boolean primitiveRestartEnable,
            int topology,

            // Raster Info
            int polygonMode,
            int cullMode,
            int frontFace,
            float lineWidth,
            boolean depthBiasEnable,
            float depthBiasConstFactor,
            float depthBiasClamp,
            float depthBiasSlopeFactor,

            // Attachments
            ArrayList<RenderPipeline.ColorAttachmentInfo> colorAttachments,
            RenderPipeline.DepthStencilAttachmentInfo depthStencilAttachment,
            int depthFormat,
            int stencilFormat,
            float[] blendConstants,

            // Shaders
            VKShaderProgram shader,
            LinkedHashMap<String, PushConstantsDefinition> pushConstants,
            DescriptorData descriptorData
    ) {}

}
