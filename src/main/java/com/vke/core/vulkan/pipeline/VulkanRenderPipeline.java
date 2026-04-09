package com.vke.core.vulkan.pipeline;

import com.vke.api.rendering.abstraction.enums.ShaderType;
import com.vke.api.rendering.vulkan.pipeline.PipelineData;
import com.vke.api.rendering.vulkan.pipeline.VertexLayoutData;
import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.abstraction.enums.texture.Format;
import com.vke.api.rendering.abstraction.pipeline.RenderPipeline;
import com.vke.api.rendering.vulkan.descriptors.DescriptorSets;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle;
import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.api.rendering.vulkan.pushconstants.PushConstantHandle;
import com.vke.api.rendering.vulkan.pushconstants.PushConstantLayout;
import com.vke.api.rendering.vulkan.pushconstants.PushConstants;
import com.vke.core.Context;
import com.vke.core.VKEngine;
import com.vke.core.services.Services;
import com.vke.core.services.shr.ReflectedShader;
import com.vke.core.services.shr.ShaderReflector;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.shader.VKShaderProgram;
import com.vke.core.vulkan.shader.VulkanShader;
import com.vke.utils.Utils;
import com.vke.utils.iter.Iter;
import com.vke.utils.iter.helpers.Option;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.*;

public class VulkanRenderPipeline implements RenderPipeline {

    private final Context context;
    private final VulkanRenderDevice device;

    private final VulkanPipelineLayout layout;

    private final long handle;

    public VulkanRenderPipeline(Context context, VulkanRenderDevice device, PipelineData data) {
        this.context = context;
        this.device = device;

        data.compiledShaders = VKShaderProgram.asVkShaderProgram(context, data.shaders);

        var shaders = getReflectedShaders(data.compiledShaders);
        DescriptorSets ds = createDescriptorSets(data, shaders);
        PushConstants pc = createPushConstants(data, shaders);
        data.vertexLayoutData = createVertexLayouts(data, shaders);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VKEngine engine = context.getEngine();
            var dynamicStates = getDynamicStates(stack, data.dynamicStates.stream().mapToInt(PipelineData.DynamicState::getVkHandle).toArray());
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
            this.layout = getPipelineLayout(context.getEngine(), device, pc, ds);

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
                engine.throwException(new IllegalStateException("Couldn't create graphics pipeline"), "GraphicsPipeline@VulkanImpl");
            }

            this.handle = pHandle.get(0);
        }
    }

    public <T extends UniformHandle> T resolveUniform(String path) {
        return this.layout.descriptors().resolve(path);
    }

    public PushConstantHandle resolvePushConstant(String path) {
        return this.layout.pushConstants().resolve(path);
    }

    public void updateUniforms(UniformHandle... uniforms) {
        this.layout.descriptors().update(uniforms);
    }



    private ArrayList<ReflectedShader> getReflectedShaders(VKShaderProgram program) {
        ShaderReflector refl = context.service(Services.SHADER_REFLECTION);
        ArrayList<ReflectedShader> reflectedShaders = new ArrayList<>();

        for (Long id : Iter.of(program.getShaders()).map(VulkanShader::getShaderID)) {
            Option<ReflectedShader> shaderOpt = refl.get(id);
            if (shaderOpt.isNone()) {
                throw new IllegalStateException("Requested reflected shader but none was found! This error should not happen");
            }
            reflectedShaders.add(shaderOpt.unwrap());
        }

        return reflectedShaders;
    }

    private DescriptorSets createDescriptorSets(PipelineData data, ArrayList<ReflectedShader> shaders) {
        HashMap<Integer, DescriptorSetLayout> sets = new HashMap<>();

        for (ReflectedShader shader : shaders) {
            var reflectedDescriptors = shader.getDescriptors();

            for (ArrayList<ReflectedShader.DescriptorResource> value : reflectedDescriptors.values()) {
                for (ReflectedShader.DescriptorResource resource : value) {
                    if (!sets.containsKey(resource.set)) sets.put(resource.set, new DescriptorSetLayout());
                }
            }

            for (Map.Entry<ReflectedShader.ResourceType, ArrayList<ReflectedShader.DescriptorResource>> entry : reflectedDescriptors.entrySet()) {
                for (ReflectedShader.DescriptorResource resource : entry.getValue()) {
                    DescriptorSetLayout descriptor = sets.computeIfAbsent(resource.set, (_) -> new DescriptorSetLayout());

                    BindingLayout binding = new BindingLayout();
                    binding.name = resource.name;
                    binding.set = resource.set;
                    binding.binding = resource.binding;
                    binding.descriptorCount = Arrays.stream(resource.arrayDim).reduce(1, (a, b) -> a * b);
                    binding.isDynamic = data.additionalDescriptorInfo.dynamicBuffers.contains(resource.name);
                    binding.type = DescriptorType.fromBaseType(entry.getKey(), binding.isDynamic);

                    binding.typeLayout = resource.struct;
                    binding.packingType = PackingType.fromDescriptorType(binding.type);

                    descriptor.bindings.add(binding);
                }
            }
        }

        return new DescriptorSets(context.getEngine(), device, (ArrayList<DescriptorSetLayout>) Iter.of(sets.values()).collectToList(), data.additionalDescriptorInfo);
    }

    private PushConstants createPushConstants(PipelineData data, ArrayList<ReflectedShader> shaders) {
        PushConstantLayout layout = null;
        for (ReflectedShader shader : shaders) {
            ReflectedShader.PushConstantsResource pc = shader.getPushConstants();
            if (pc == null) continue;

            layout = new PushConstantLayout(pc.name, 0, pc.size, pc.struct, PackingType.STD140);
        }

        if (layout == null) layout = new PushConstantLayout("", 0, 0, null, PackingType.STD140);

        return new PushConstants(layout);
    }

    private VertexLayoutData createVertexLayouts(PipelineData data, ArrayList<ReflectedShader> shaders) {
        ArrayList<VertexLayoutData.Attribute> attribs = new ArrayList<>();

        for (ReflectedShader shader : shaders) {
            if (shader.getShaderType() == ShaderType.FRAGMENT) continue;
            var vaos = shader.getVAOs();

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

    private VkPipelineRenderingCreateInfo getRenderingInfo(MemoryStack stack, PipelineData data, IntBuffer attachmentFormats) {
        return VkPipelineRenderingCreateInfo.calloc(stack)
                .sType$Default()
                .colorAttachmentCount(data.colorAttachments.size())
                .pColorAttachmentFormats(attachmentFormats)
                .depthAttachmentFormat(data.depthAttachment == null ? 0 : data.depthAttachment.format.getVkHandle())
                .stencilAttachmentFormat(data.stencilAttachment == null ? 0 : data.stencilAttachment.format.getVkHandle());
    }

    private VkPipelineShaderStageCreateInfo.Buffer getShaderStages(MemoryStack stack, PipelineData data) {
        VkPipelineShaderStageCreateInfo.Buffer stages =
                VkPipelineShaderStageCreateInfo.calloc(data.shaders.getShaderCount(), stack);

        VkPipelineShaderStageCreateInfo[] shaderStageCreateInfos = data.compiledShaders.getShaderCreateInfos();

        for (int i = 0; i < shaderStageCreateInfos.length; i++) {
            VkPipelineShaderStageCreateInfo stage = shaderStageCreateInfos[i];

            stages.get(i).sType$Default()
                    .stage(stage.stage())
                    .module(stage.module())
                    .pName(Utils.ensureCStr(stage.pName()));
        }
        return stages;
    }

    private VkPipelineViewportStateCreateInfo getViewportInfo(MemoryStack stack) {
        return VkPipelineViewportStateCreateInfo.calloc(stack)
                .sType$Default()
                .viewportCount(1)
                .scissorCount(1);
    }

    private VulkanPipelineLayout getPipelineLayout(VKEngine engine, VulkanRenderDevice device, PushConstants pc, DescriptorSets ds) {
        return VulkanPipelineLayout.getLayout(engine, device, pc, ds);
    }
    //endregion

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
