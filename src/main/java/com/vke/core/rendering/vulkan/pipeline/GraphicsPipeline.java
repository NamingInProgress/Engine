package com.vke.core.rendering.vulkan.pipeline;

import com.vke.api.vulkan.PipelineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.rendering.vulkan.swapchain.SwapChain;
import com.vke.utils.Disposable;
import com.vke.utils.Utils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

public class GraphicsPipeline implements Disposable {
    private static String HERE = "GraphicsPipeline";

    private long handle;
    private LogicalDevice device;
    private VKEngine engine;
    private SwapChain swapChain;

    public GraphicsPipeline(PipelineCreateInfo createInfo) {
        this.device = createInfo.device;
        this.engine = createInfo.engine;
        this.swapChain = createInfo.swapChain;

        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer dynamicStates = stack.ints(
                    VK14.VK_DYNAMIC_STATE_VIEWPORT, VK14.VK_DYNAMIC_STATE_SCISSOR
            );

            VkPipelineDynamicStateCreateInfo dynamicStateCreateInfo = VkPipelineDynamicStateCreateInfo.calloc(stack)
                    .sType$Default()
                    .pDynamicStates(dynamicStates);

            VkPipelineVertexInputStateCreateInfo vertexInputStateCreateInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack)
                    .sType$Default();
            //TODO: actually describe inputs later when we use vbo

            VkPipelineInputAssemblyStateCreateInfo inputAssemblyStateCreateInfo = VkPipelineInputAssemblyStateCreateInfo.calloc(stack)
                    .sType$Default();
            //TODO replace with smth from create info probably
            inputAssemblyStateCreateInfo.topology(VK14.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);

            VkPipelineRasterizationStateCreateInfo rasterizationStateCreateInfo = VkPipelineRasterizationStateCreateInfo.calloc(stack)
                    .sType$Default()
                    .depthClampEnable(false)
                    .rasterizerDiscardEnable(false)
                    .polygonMode(VK14.VK_POLYGON_MODE_FILL)
                    .cullMode(VK14.VK_CULL_MODE_BACK_BIT)
                    .frontFace(VK14.VK_FRONT_FACE_CLOCKWISE)
                    .depthBiasEnable(false)
                    .depthBiasSlopeFactor(1.0f)
                    .lineWidth(1.0f);

            VkPipelineMultisampleStateCreateInfo multisampleStateCreateInfo = VkPipelineMultisampleStateCreateInfo.calloc(stack)
                    .sType$Default()
                    .rasterizationSamples(VK14.VK_SAMPLE_COUNT_1_BIT)
                    .sampleShadingEnable(false);

            VkPipelineColorBlendAttachmentState colorBlendAttachment = VkPipelineColorBlendAttachmentState.calloc(stack)
                    .colorWriteMask(VK14.VK_COLOR_COMPONENT_R_BIT | VK14.VK_COLOR_COMPONENT_G_BIT | VK14.VK_COLOR_COMPONENT_B_BIT | VK14.VK_COLOR_COMPONENT_A_BIT)
                    .blendEnable(true)
                    .srcColorBlendFactor(VK14.VK_BLEND_FACTOR_SRC_ALPHA)
                    .dstColorBlendFactor(VK14.VK_BLEND_FACTOR_ONE_MINUS_DST_ALPHA)
                    .colorBlendOp(VK14.VK_BLEND_OP_ADD)
                    .srcAlphaBlendFactor(VK14.VK_BLEND_FACTOR_ONE)
                    .dstAlphaBlendFactor(VK14.VK_BLEND_FACTOR_ZERO);

            VkPipelineColorBlendAttachmentState.Buffer attachments = VkPipelineColorBlendAttachmentState.calloc(1, stack);
            attachments.put(colorBlendAttachment);
            VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.calloc(stack)
                    .sType$Default()
                    .attachmentCount(1)
                    .logicOpEnable(false)
                    .logicOp(VK14.VK_LOGIC_OP_COPY)
                    .pAttachments(attachments)
                    .attachmentCount(1);

            PipelineLayout pipelineLayout = new PipelineLayout(engine, device);

            IntBuffer format = stack.ints(swapChain.getColorFormat());
            VkPipelineRenderingCreateInfo renderingCreateInfo = VkPipelineRenderingCreateInfo.calloc(stack)
                    .sType$Default()
                    .pColorAttachmentFormats(format)
                    .colorAttachmentCount(1)
                    .depthAttachmentFormat(VK14.VK_FORMAT_UNDEFINED)
                    .stencilAttachmentFormat(VK14.VK_FORMAT_UNDEFINED);

            VkPipelineShaderStageCreateInfo[] stagesArr = createInfo.shaderModuleCreateInfos;

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
        }
    }

    public long getHandle() {
        return this.handle;
    }

    @Override
    public void free() {
        VK14.vkDestroyPipeline(device.getDevice(), handle, null);
    }
}
