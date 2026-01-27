package com.vke.core.rendering.vulkan;

import com.vke.api.vulkan.createInfos.PipelineCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.commands.CommandBuffers;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.rendering.vulkan.pipeline.GraphicsPipeline;
import com.vke.core.rendering.vulkan.shader.Shader;
import com.vke.core.rendering.vulkan.shader.ShaderCompiler;
import com.vke.core.rendering.vulkan.shader.ShaderProgram;
import com.vke.core.rendering.vulkan.swapchain.SwapChain;
import com.vke.core.rendering.vulkan.sync.Fence;
import com.vke.core.rendering.vulkan.sync.Semaphore;
import com.vke.utils.Disposable;
import com.vke.utils.Identifier;
import com.vke.utils.Utils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.Shaderc;
import org.lwjgl.vulkan.*;

public class VulkanRenderer implements Disposable {
    private static final int FENCE_TIMEOUT = 1000000000;

    private final VKEngine engine;
    private final VulkanSetup setup;
    private GraphicsPipeline pipeline;
    private final SwapChain swapChain;
    private final ShaderCompiler shaderCompiler;
    private int frame;
    private final int frameCount;
    private final int framesInFlight;

    public VulkanRenderer(VKEngine engine, EngineCreateInfo createInfo, int framesInFlight) {
        this.engine = engine;
        this.setup = new VulkanSetup(createInfo);
        this.framesInFlight = framesInFlight;
        setup.initVulkan(engine);
        this.shaderCompiler = new ShaderCompiler();
        this.swapChain = setup.getSwapChain();
        this.frameCount = swapChain.getImageCount();

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
            //pipelineCreateInfo.shader = program;
            pipelineCreateInfo.swapChain = swapChain;
            //pipelineCreateInfo.shaderModuleCreateInfos = program.getShaderCreateInfos();

            pipeline = new GraphicsPipeline(pipelineCreateInfo,null);
        } catch (Exception e) {
            engine.throwException(e, "Vulkan Renderer");
        }
    }

    public void draw() {
        VK14.vkDeviceWaitIdle(setup.getLogicalDevice().getDevice());
        try(MemoryStack stack = MemoryStack.stackPush()) {
            Frame f = setup.getFrames()[frame % framesInFlight];
            Fence fence = f.getRenderFence();

            fence.waitAndReset(stack, engine, setup.getLogicalDevice(), FENCE_TIMEOUT);

            int imageIdx = swapChain.nextImage(stack, f.getSwapChainSemaphore(), null);
            CommandBuffers cmd = f.getBuffers();
            cmd.reset();

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

            //submit queue
            VkCommandBufferSubmitInfo cmdSubmitInfo = CommandBuffers.getDefaultSubmitInfo(stack, cmd);

            VkSemaphoreSubmitInfo waitInfo = Semaphore.getDefaultSubmitInfo(stack, f.getSwapChainSemaphore(), (int) VK14.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT);
            VkSemaphoreSubmitInfo signalInfo = Semaphore.getDefaultSubmitInfo(stack, f.getRenderSemaphore(), (int) VK14.VK_PIPELINE_STAGE_2_ALL_GRAPHICS_BIT);

            VkSubmitInfo2 submitInfo = createSubmitInfo2(stack, cmdSubmitInfo, waitInfo, signalInfo);
            VkSubmitInfo2.Buffer submitBuf = VkSubmitInfo2.calloc(1, stack);
            submitBuf.put(0, submitInfo);

            VkQueue graphicsQueue = setup.getLogicalDevice().getQueue(VulkanQueue.Type.GRAPHICS).vk();
            if (VK14.vkQueueSubmit2(graphicsQueue, submitBuf, fence.getHandle()) != VK14.VK_SUCCESS) {
                engine.getLogger().warn("Failed to submit queue at frame " + frameCount);
            }

            //present queue
            VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack);
            presentInfo.sType$Default();
            presentInfo.pImageIndices(stack.ints(imageIdx));
            presentInfo.pSwapchains(stack.longs(swapChain.handle()));
            presentInfo.pWaitSemaphores(stack.longs(f.getRenderSemaphore().getHandle()));
            presentInfo.swapchainCount(1);

            if (KHRSwapchain.vkQueuePresentKHR(graphicsQueue, presentInfo) != VK14.VK_SUCCESS) {
                engine.getLogger().warn("Failed to present queue at frame " + frameCount);
            }

        }

        frame++;
    }

    private VkSubmitInfo2 createSubmitInfo2(MemoryStack stack, VkCommandBufferSubmitInfo cmdInfo, VkSemaphoreSubmitInfo wait, VkSemaphoreSubmitInfo signal) {
        VkCommandBufferSubmitInfo.Buffer cmdBuf = VkCommandBufferSubmitInfo.calloc(1, stack);
        cmdBuf.put(0, cmdInfo);

        VkSemaphoreSubmitInfo.Buffer waitBuf = VkSemaphoreSubmitInfo.calloc(1, stack);
        waitBuf.put(0, wait);

        VkSemaphoreSubmitInfo.Buffer signalBuf = VkSemaphoreSubmitInfo.calloc(1, stack);
        signalBuf.put(0, signal);

        VkSubmitInfo2 info = VkSubmitInfo2.calloc(stack);
        info.sType$Default();
        info.pCommandBufferInfos(cmdBuf);
        info.pWaitSemaphoreInfos(waitBuf);
        info.pSignalSemaphoreInfos(signalBuf);

        return info;
    }

    @Override
    public void free() {
        setup.free();
    }
}
