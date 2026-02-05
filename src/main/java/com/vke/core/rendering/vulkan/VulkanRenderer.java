package com.vke.core.rendering.vulkan;

import com.vke.api.registry.VKERegistries;
import com.vke.api.services.Service;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.commands.CommandBuffers;
import com.vke.core.rendering.vulkan.swapchain.SwapChain;
import com.vke.core.rendering.vulkan.sync.Fence;
import com.vke.core.services.Services;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.util.List;
import java.util.function.BiConsumer;

public class VulkanRenderer extends Service {
    private static final int FENCE_TIMEOUT = 1000000000;

    private final VKEngine engine;
    private final VulkanSetup setup;
    private final SwapChain swapChain;
    private int frame;
    private final int frameCount;
    private final int framesInFlight;

    public VulkanRenderer(VKEngine engine, EngineCreateInfo createInfo) {
        super("vkr");
        this.engine = engine;
        this.setup = new VulkanSetup(createInfo);
        this.framesInFlight = createInfo.vulkanCreateInfo.framesInFlight;
        setup.initVulkan(engine);
        VKERegistries.PIPELINES.makeVkPipelines(engine, setup);
        this.swapChain = setup.getSwapChain();
        this.frameCount = swapChain.getImageCount();
    }

    public FrameData setupFrame() {
        VK14.vkDeviceWaitIdle(setup.getLogicalDevice().getDevice());
        MemoryStack stack = MemoryStack.stackPush();
        Frame f = setup.getFrames()[frame % framesInFlight];
        Fence fence = f.getRenderFence();

        fence.waitAndReset(stack, engine, setup.getLogicalDevice(), FENCE_TIMEOUT);

        int imageIdx = swapChain.nextImage(stack, f.getSwapChainSemaphore(), null);
        CommandBuffers cmd = f.getBuffers();
        cmd.reset();

        cmd.startRecording(stack, swapChain);

        return new FrameData(imageIdx, cmd, f, this.swapChain, stack);
    }

    public void endFrame(FrameData bfData) {
        CommandBuffers cmd = bfData.cmd();
        Frame f = bfData.frame();
        int imageIdx = bfData.imageIndex();
        MemoryStack stack = bfData.getStack();

        cmd.endRecording(swapChain);

        // submit hitoasnfoasd

        VulkanQueue graphicsQueueVKE = setup.getLogicalDevice().getQueue(VulkanQueue.Type.GRAPHICS);
        graphicsQueueVKE.submit(engine, stack, setup, f, frame);

        VkQueue graphicsQueue = graphicsQueueVKE.vk();

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

        stack.close();

        frame++;
    }

    public void immediateSubmit(BiConsumer<MemoryStack, CommandBuffers> consumer) {
        ImmediateFrame f = setup.getImmediateFrame();
        CommandBuffers icmd = f.getBuffers();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            Fence fence = f.getFence();
            fence.reset(stack, engine, setup.getLogicalDevice());
            //f.getRenderFence().waitAndReset(stack, engine, setup.getLogicalDevice(), FENCE_TIMEOUT);
            icmd.reset();

            icmd.startRecordingImmediate(stack);
            consumer.accept(stack, icmd);
            icmd.endRecordingImmediate();

            VulkanQueue q = setup.getLogicalDevice().getQueue(VulkanQueue.Type.TRANSFER);
            q.submitImmediate(engine, stack, f);

            //wait for fence
            fence.waitForFence(stack, engine, setup.getLogicalDevice(), FENCE_TIMEOUT);
        }
    }

    public VulkanSetup getSetup() { return this.setup; }

    @Override
    public void free() {
        VK14.vkDeviceWaitIdle(setup.getLogicalDevice().getDevice());

        setup.free();
    }

    @Override
    public List<String> dependencies() {
        return List.of(Services.SHADER_COMPILER);
    }


    public static final class FrameData {
        private final int imageIndex;
        private final CommandBuffers cmd;
        private final Frame frame;
        private final SwapChain swapchain;
        private MemoryStack stack;

        public FrameData(int imageIndex, CommandBuffers cmd, Frame frame, SwapChain swapchain, MemoryStack stack) {
            this.imageIndex = imageIndex;
            this.cmd = cmd;
            this.frame = frame;
            this.stack = stack;
            this.swapchain = swapchain;
        }

        private int imageIndex() {
            return imageIndex;
        }

        public CommandBuffers cmd() {
            return cmd;
        }

        private Frame frame() {
            return frame;
        }

        public SwapChain swapChain() {
            return this.swapchain;
        }

        public MemoryStack getStack() {
            return this.stack;
        }
    }
}
