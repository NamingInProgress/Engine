package com.vke.core.vulkan;

import com.vke.api.rendering.FrameCounter;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.command.CommandPool;
import com.vke.core.vulkan.device.LogicalDevice;
import com.vke.api.rendering.abstraction.enums.QueueType;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.service.VulkanRenderSystem;
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
    private final VulkanRenderSystem sys;

    public VulkanFrame(VulkanRenderSystem sys, FrameCounter fc) {
        this(sys, fc, false);
    }

    public VulkanFrame(VulkanRenderSystem sys, FrameCounter fc, boolean immediate) {
        this.sys = sys;
        pool = new CommandPool(sys, immediate ? QueueType.TRANSFER : QueueType.GRAPHICS);
        buffers = new VulkanCmdBuffers(sys, pool, fc);

        setupSyncStructures(immediate);
    }

    private void setupSyncStructures(boolean immediate) {
        try {
            if (!immediate) {
                imageSemaphore = VulkanSemaphore.createSemaphore(sys);
                presentSemaphore = VulkanSemaphore.createSemaphore(sys);
            }

            renderFence = new VulkanFence(sys);
        } catch (Throwable t) {
            sys.throwException(t, "VulkanFrame");
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
