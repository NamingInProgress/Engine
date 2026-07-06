package com.vke.core.vulkan;

import com.vke.api.rendering.FrameCounter;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.command.CommandPool;
import com.vke.core.vulkan.device.LogicalDevice;
import com.vke.api.rendering.abstraction.enums.QueueType;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.swapchain.VulkanSwapchain;
import com.vke.core.vulkan.sync.VulkanFence;
import com.vke.core.vulkan.sync.VulkanSemaphore;
import com.vke.utils.io.Disposable;
import org.jetbrains.annotations.Nullable;

public class VulkanFrame implements Disposable {

    private CommandPool pool;
    private VulkanCmdBuffers buffers;
    private VulkanSemaphore imageSemaphore, presentSemaphore;
    private VulkanFence renderFence;

    public VulkanFrame(VKEngine engine, VulkanRenderDevice device, VulkanSwapchain swapchain, FrameCounter fc) {
        this(engine, device, swapchain, fc, false);
    }

    public VulkanFrame(VKEngine engine, VulkanRenderDevice device, VulkanSwapchain swapchain, FrameCounter fc, boolean immediate) {
        pool = new CommandPool(engine, device.getLogicalDevice(), immediate ? QueueType.TRANSFER : QueueType.GRAPHICS);
        buffers = new VulkanCmdBuffers(engine, device, swapchain, pool, fc);

        setupSyncStructures(engine, device.getLogicalDevice(), immediate);
    }

    private void setupSyncStructures(VKEngine engine, LogicalDevice device, boolean immediate) {
        try {
            if (!immediate) {
                imageSemaphore = VulkanSemaphore.createSemaphore(engine, device);
                presentSemaphore = VulkanSemaphore.createSemaphore(engine, device);
            }

            renderFence = new VulkanFence(device);
        } catch (Throwable t) {
            engine.throwException(t, "VulkanFrame");
        }
    }

    public CommandPool getPool() {
        return pool;
    }

    public VulkanCmdBuffers getBuffers() {
        return buffers;
    }

    public @Nullable VulkanSemaphore getImageSemaphore() {
        return imageSemaphore;
    }

    public @Nullable VulkanSemaphore getPresentSemaphore() {
        return presentSemaphore;
    }

    public VulkanFence getRenderFence() {
        return renderFence;
    }

    @Override
    public void free() {
        if (imageSemaphore != null)
            imageSemaphore.free();
        if (presentSemaphore != null)
            presentSemaphore.free();
        renderFence.free();
        buffers.free();
        pool.free();
    }

}
