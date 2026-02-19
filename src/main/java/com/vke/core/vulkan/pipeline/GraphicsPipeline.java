package com.vke.core.vulkan.pipeline;

import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.carrotsearch.hppc.cursors.ObjectIntCursor;
import com.vke.api.abstraction.data.Sampler;
import com.vke.api.abstraction.data.Texture;
import com.vke.api.vulkan.ImageLayout;
import com.vke.core.vulkan.createInfos.PipelineCreateInfo;
import com.vke.api.vulkan.descriptors.DescriptorData;
import com.vke.api.vulkan.pipeline.PushConstantsDefinition;
import com.vke.api.vulkan.pipeline.RenderPipeline;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.buffers.premade.BufferSlice;
import com.vke.core.vulkan.VKUtils;
import com.vke.core.vulkan.descriptor.*;
import com.vke.core.vulkan.sampler.VulkanSampler;
import com.vke.core.vulkan.shader.VKShaderProgram;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.texture.VulkanImage;
import com.vke.core.vulkan.texture.VulkanTexture;
import com.vke.utils.Disposable;
import com.vke.utils.Pair;
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
    private VulkanRenderDevice device;
    private VKEngine engine;
    private PipelineLayout layout;
    private List<DescriptorSetLayout> descriptorSetLayouts = new ArrayList<>();
    private List<DescriptorSet> descriptorSets = new ArrayList<>();

    private DescriptorAllocator descAlloc;

    public GraphicsPipeline(PipelineCreateInfo createInfo, PipelineSettingsInfo pipelineSettingsInfo) {
        this.device = createInfo.device;
        this.engine = createInfo.engine;

        DescriptorData descriptorData = pipelineSettingsInfo.descriptorData;

        if (descriptorData != null) {
            List<DescriptorPool.DescriptorTypeCountInfo> descriptorTypeCountInfo = new ArrayList<>();

            for (ObjectIntCursor<DescriptorType> count : descriptorData.counts()) {
                descriptorTypeCountInfo.add(new DescriptorPool.DescriptorTypeCountInfo(count.value, count.key)); // might not work probab;ly will
            }

            for (IntObjectCursor<DescriptorData.Set> set : descriptorData.getSets()) {
                DescriptorSetLayout.Builder builder = new DescriptorSetLayout.Builder();

                builder.fromWrapper(set.value.getBindings());

                descriptorSetLayouts.add(builder.build(engine, device.getLogicalDevice()));
            }

            if (descriptorData.getSetsAmount() != 0) {
                DescriptorPoolCreateInfo poolCreateInfo = new DescriptorPoolCreateInfo();
                poolCreateInfo.maxSets = descriptorData.getSetsAmount();
                poolCreateInfo.engine = engine;
                poolCreateInfo.device = device;
                poolCreateInfo.descriptorTypeCountInfo = descriptorTypeCountInfo;

                descAlloc = new DescriptorAllocator(poolCreateInfo);

                for (int i = 0; i < descriptorSetLayouts.size(); i++) {
                    descriptorSets.add(i, descAlloc.allocate(descriptorSetLayouts.get(i)));
                }
            }
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


            PipelineLayout pipelineLayout = new PipelineLayout(engine, device.getLogicalDevice(), pipelineSettingsInfo.pushConstants(), descriptorData, descriptorSetLayouts);
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

            if (VK14.vkCreateGraphicsPipelines(device.getLogicalDevice().getDevice(), VK14.VK_NULL_HANDLE, pipelineInfos, null, pPipeline) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Couldn't create graphics pipeline"), HERE);
            }

            this.handle = pPipeline.get(0);
            if (engine.isDebugMode()) {
                if (!VKUtils.setDebugName(device.getLogicalDevice(), createInfo.name, this.handle, VK14.VK_OBJECT_TYPE_PIPELINE)) {
                    engine.throwException(new IllegalStateException("Couldn't set debug name"), HERE);
                }
            }
        }
    }

    public List<DescriptorSet> getDescriptorSets() {
        return descriptorSets;
    }

    public long getHandle() { return this.handle; }
    public PipelineLayout getPipelineLayout() { return this.layout; }
    public DescriptorData getDescriptorData() { return this.getPipelineLayout().getDescriptors(); }

    public void setUniform(String name, Consumer<BufferSlice> runnable) {
        Pair<Integer, Integer> pos = getDescriptorData().getPosition(name);

        DescriptorBinding b = this.descriptorSets.get(pos.v1).getBinding(pos.v2);
        if (b instanceof DescriptorBinding.BufferBinding bb) {
            bb.write(name, runnable);
        } else {
            engine.throwException(new IllegalStateException("Tried to modify buffer on descriptor binding which is not of buffer type"), HERE);
        }
    }

    public void setSampler(String name, Sampler sampler, Texture tex) {
        Pair<Integer, Integer> pos = getDescriptorData().getPosition(name);

        DescriptorBinding b = this.descriptorSets.get(pos.v1).getBinding(pos.v2);
        if (b instanceof DescriptorBinding.SamplerBinding sb) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                sb.setSampler((VulkanSampler) sampler);
                sb.setImageView((VulkanTexture) tex);


                this.descriptorSets.get(pos.v1).updateImage(stack, pos.v2, b, b.getBindingInfo(stack));
            }
        }
    }

    public void setImage(String name, long imageView) {
        this.setImage(name, imageView, ImageLayout.GENERAL);
    }

    public void setImage(String name, long imageView, ImageLayout layout) {
        Pair<Integer, Integer> pos = getDescriptorData().getPosition(name);

        DescriptorBinding b = this.descriptorSets.get(pos.v1).getBinding(pos.v2);
        if (b instanceof DescriptorBinding.ImageBinding ib) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                ib.setImageView(imageView);
                ib.setImageLayout(layout);

                this.descriptorSets.get(pos.v1).updateImage(stack, pos.v2, b, b.getBindingInfo(stack));
            }
        }
    }

    @Override
    public void free() {
        layout.free();
        descriptorSets.forEach(DescriptorSet::free);
        descriptorSetLayouts.forEach(DescriptorSetLayout::free);
        if (descAlloc != null) descAlloc.free();
        VK14.vkDestroyPipeline(device.getLogicalDevice().getDevice(), handle, null);
    }

    public com.vke.api.abstraction.pipeline.PipelineLayout layout() {
        return this.layout;
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
