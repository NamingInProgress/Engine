package com.vke.core.vulkan;

import com.vke.api.abstraction.swapchain.Swapchain;
import com.vke.api.registry.VKERegistries;
import com.vke.api.services.Service;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.commands.CommandBuffers;
import com.vke.core.services.Services;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.swapchain.VulkanSwapchain;
import com.vke.core.vulkan.sync.VulkanFence;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK14;

import java.util.Arrays;
import java.util.List;

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
        this.frames = device.createFrames();
        this.immediateFrame = device.createImmediateFrame();
        this.imagesInFlight = new VulkanFence[this.swapchain.getImageCount()];

        VKERegistries.PIPELINES.makeVkPipelines(engine, device);
    }

    public FrameData startFrame() {
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

        CommandBuffers cmd = frame.getBuffers();
        cmd.reset();

        cmd.startRecording(stack, swapchain);

        return new FrameData(frame, stack, imageIndex);
    }

    public void endFrame(FrameData frameData) {
        CommandBuffers cmd = frameData.frame().getBuffers();

        cmd.endRecording(swapchain);

        // TODO: SUBMIT QUEUE
        swapchain.present(frameData.frame().getPresentSemaphore());

        frameData.stack().close();
        currentFrame++;
    }

    @Override
    protected List<String> dependencies() {
        return List.of(Services.SHADER_COMPILER);
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
