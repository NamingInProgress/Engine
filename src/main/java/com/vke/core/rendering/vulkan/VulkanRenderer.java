package com.vke.core.rendering.vulkan;

import com.sun.source.tree.UnaryTree;
import com.vke.api.vulkan.PipelineCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.commands.CommandBuffers;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.rendering.vulkan.pipeline.GraphicsPipeline;
import com.vke.core.rendering.vulkan.shader.Shader;
import com.vke.core.rendering.vulkan.shader.ShaderCompiler;
import com.vke.core.rendering.vulkan.shader.ShaderProgram;
import com.vke.core.rendering.vulkan.swapchain.SwapChain;
import com.vke.utils.Disposable;
import com.vke.utils.Identifier;
import com.vke.utils.Utils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.Shaderc;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkViewport;

import java.awt.*;

public class VulkanRenderer implements Disposable {
    private final VulkanSetup setup;
    private GraphicsPipeline pipeline;
    private SwapChain swapChain;
    private final ShaderCompiler shaderCompiler;

    public VulkanRenderer(VKEngine engine, EngineCreateInfo createInfo) {
        this.setup = new VulkanSetup(createInfo);
        setup.initVulkan(engine);
        this.shaderCompiler = new ShaderCompiler();
        this.swapChain = setup.getSwapChain();

        try {
            byte[] vertexSource = Utils.readAllBytesAndClose(new Identifier("vke", "shaders/shader.vsh").asInputStream());
            byte[] fragSource = Utils.readAllBytesAndClose(new Identifier("vke", "shaders/shader.fsh").asInputStream());

            LogicalDevice logicalDevice = setup.getLogicalDevice();
            SwapChain swapChain = setup.getSwapChain();
            //TOY example test
            Shader vertexShader = new Shader(engine, logicalDevice, shaderCompiler.compileGlslToSpirV(vertexSource, Shaderc.shaderc_vertex_shader, "shader.vsh"), Shader.Type.VERTEX);
            Shader fragmentShader = new Shader(engine, logicalDevice, shaderCompiler.compileGlslToSpirV(fragSource, Shaderc.shaderc_fragment_shader, "shader.fsh"), Shader.Type.FRAGMENT);

            ShaderProgram program = new ShaderProgram(vertexShader, fragmentShader);

            PipelineCreateInfo pipelineCreateInfo = new PipelineCreateInfo();
            pipelineCreateInfo.device = logicalDevice;
            pipelineCreateInfo.engine = engine;
            pipelineCreateInfo.shader = program;
            pipelineCreateInfo.swapChain = swapChain;
            pipelineCreateInfo.shaderModuleCreateInfos = program.getShaderCreateInfos();

            pipeline = new GraphicsPipeline(pipelineCreateInfo);
        } catch (Exception e) {
            engine.throwException(e, "Vulkan Renderer");
        }
    }

    public void draw() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            swapChain.nextImage(stack);
            CommandBuffers cmd = setup.getCommandBuffers();
            cmd.startRecording(stack, swapChain);

            //rendering code

            VkViewport.Buffer viewportBuffer = VkViewport.calloc(1, stack);
            viewportBuffer.get(0)
                    .set(0, 0, swapChain.getExtent().width(), swapChain.getExtent().height(), 0, 1);

            VkRect2D.Buffer scissorBuffer = VkRect2D.calloc(1, stack);
            scissorBuffer.get(0)
                            .set(VkRect2D.calloc(stack)
                                    .extent(swapChain.getExtent())
                            );

            cmd.bindPipeline(pipeline, VK14.VK_PIPELINE_BIND_POINT_GRAPHICS);
            cmd.setViewport(0, viewportBuffer);
            cmd.setScissor(0, scissorBuffer);

            VK14.vkCmdDraw(cmd.getBuffer(), 3, 1, 0, 0);

            cmd.endRecording(swapChain);
        }
    }

    @Override
    public void free() {
        setup.free();
    }
}
