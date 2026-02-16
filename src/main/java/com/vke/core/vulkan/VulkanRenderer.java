package com.vke.core.vulkan;

import com.vke.api.abstraction.commands.CommandBuffer;
import com.vke.api.abstraction.descriptors.QueueType;
import com.vke.api.abstraction.swapchain.Swapchain;
import com.vke.api.registry.VKERegistries;
import com.vke.api.services.Service;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.services.Services;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.swapchain.VulkanSwapchain;
import com.vke.core.vulkan.sync.VulkanFence;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class VulkanRenderer extends Service {

    private static final String HERE = "VulkanRenderer";

    private final int FRAMES_IN_FLIGHT;

    private final VulkanSwapchain swapchain;
    private final VulkanRenderDevice device;
    private final VulkanFrame[] frames;
    private final VulkanFence[] imagesInFlight;

    private final VulkanFrame immediateFrame;

    private int currentFrame = 0;

    // Engine infos
    private final VKEngine engine;
    private final EngineCreateInfo engineCreateInfo;

    public VulkanRenderer(VKEngine engine, EngineCreateInfo createInfo) {
        super("vkr");
        this.FRAMES_IN_FLIGHT = createInfo.vulkanCreateInfo.framesInFlight;
        this.engine = engine;
        this.engineCreateInfo = createInfo;
        this.device = new VulkanRenderDevice(engine, createInfo);
        this.swapchain = device.createSwapchain(
                new Swapchain.Description(createInfo.vsync, engine.getWindow().getHandle()));
        this.frames = device.createFrames(swapchain);
        this.immediateFrame = device.createImmediateFrame(swapchain);
        this.imagesInFlight = new VulkanFence[this.swapchain.getImageCount()];

        VKERegistries.PIPELINES.makeVkPipelines(engine, device);
    }

    public FrameData startFrame() {
        // TODO: Remove this and replace with better semaphore handling
        device.waitIdle();
        MemoryStack stack = MemoryStack.stackPush();

        VulkanFrame frame = frames[currentFrame];
        VulkanFence fence = frame.getRenderFence();

        fence.waitForFence();
        fence.reset();

        int imageIndex = swapchain.acquireNextImage(frame.getImageSemaphore());
        if (imageIndex < 0) {
            int errorCode = ~imageIndex;
            if (errorCode == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
                swapchain.recreate();
                stack.close();
                return null;
            }
        }

        if (imagesInFlight[imageIndex] != null) {
            imagesInFlight[imageIndex].waitForFence();
        }

        imagesInFlight[imageIndex] = fence;

        VulkanCmdBuffers cmd = frame.getBuffers();
        cmd.reset();

        cmd.begin();

        return new FrameData(frame, stack, imageIndex);
    }

    public void endFrame(FrameData frameData) {
        VulkanCmdBuffers cmd = frameData.frame().getBuffers();

        cmd.end();

        device.submit(cmd, new CommandBuffer.SubmitInfo(
                frameData.frame.getImageSemaphore(),
                frameData.frame.getPresentSemaphore(),
                frameData.frame.getRenderFence(),
                QueueType.GRAPHICS,
                false
        ));

        swapchain.present(frameData.frame().getPresentSemaphore());

        frameData.stack().close();
        currentFrame = (currentFrame + 1) % FRAMES_IN_FLIGHT;
    }

    public void immediateSubmit(BiConsumer<MemoryStack, VulkanCmdBuffers> consumer) {
        this.immediateSubmit(consumer, () -> {});
    }

    public void immediateSubmit(BiConsumer<MemoryStack, VulkanCmdBuffers> consumer, Runnable finisher) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VulkanFence fence = immediateFrame.getRenderFence();
            VulkanCmdBuffers cmd = immediateFrame.getBuffers();

            fence.reset();
            cmd.reset();

            cmd.beginImmediate();
            consumer.accept(stack, cmd);
            cmd.endImmediate();

            device.submit(cmd, CommandBuffer.SubmitInfo.immediate(fence));

            fence.waitForFence();
            finisher.run();
        }
    }

    @Override
    protected List<String> dependencies() {
        return List.of(Services.SHADER_COMPILER);
    }

    public VulkanRenderDevice getDevice() {
        return this.device;
    }

    @Override
    public void free() {
        VKERegistries.PIPELINES.freeVkPipelines();
        Arrays.stream(frames).forEach(VulkanFrame::free);
        this.immediateFrame.free();
        this.swapchain.free();
        this.device.free();
    }

    public record FrameData(VulkanFrame frame, MemoryStack stack, int imageIndex) {}

}
