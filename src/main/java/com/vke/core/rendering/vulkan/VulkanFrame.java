package com.vke.core.rendering.vulkan;

import com.vke.core.rendering.vulkan.command.CommandPool;
import com.vke.api.rendering.abstraction.renderer.enums.QueueType;
import com.vke.core.rendering.vulkan.command.VulkanCmdBuffers;
import com.vke.core.rendering.vulkan.device.VulkanRenderDevice;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import com.vke.core.rendering.vulkan.sync.VulkanFence;
import com.vke.core.rendering.vulkan.sync.VulkanSemaphore;
import com.vke.utils.io.Disposable;
import org.jetbrains.annotations.Nullable;

public class VulkanFrame implements Disposable {

    private final CommandPool pool, presentPool;
    private final VulkanCmdBuffers buffers, presentBuffers;
    private VulkanSemaphore imageSemaphore, presentSemaphore, presentTransferComplete;
    private VulkanFence renderFence, presentFence;
    private final VulkanRenderSystem sys;

    private final boolean separatePresentQueue;

    public VulkanFrame(VulkanRenderSystem sys) {
        this.sys = sys;
        pool = new CommandPool(sys, QueueType.GRAPHICS);
        buffers = new VulkanCmdBuffers(sys, pool);

        VulkanRenderDevice dev = sys.device();
        if (dev.isSeparateGraphicsPresent()) {
            this.presentPool = new CommandPool(sys, QueueType.PRESENT);
            this.presentBuffers = new VulkanCmdBuffers(sys, presentPool);
            this.separatePresentQueue = true;
        } else {
            this.presentPool = null;
            this.presentBuffers = null;
            this.separatePresentQueue = false;
        }

        setupSyncStructures();
    }

    private void setupSyncStructures() {
        try {
            imageSemaphore = VulkanSemaphore.createSemaphore(sys);
            presentSemaphore = VulkanSemaphore.createSemaphore(sys);
            if (separatePresentQueue) {
                presentTransferComplete = VulkanSemaphore.createSemaphore(sys);
                presentFence = new VulkanFence(sys);
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

    public CommandPool getPresentPool() {
        return presentPool;
    }

    public VulkanCmdBuffers getPresentBuffers() {
        return presentBuffers;
    }

    public @Nullable VulkanSemaphore getImageSemaphore() {
        return imageSemaphore;
    }

    public @Nullable VulkanSemaphore getPresentSemaphore() {
        return presentSemaphore;
    }

    public @Nullable VulkanSemaphore getTransferSemaphore() {
        return presentTransferComplete;
    }

    public VulkanFence getRenderFence() {
        return renderFence;
    }

    public @Nullable VulkanFence getPresentFence() { return this.presentFence; }

    @Override
    public void free() {
        if (imageSemaphore != null)
            imageSemaphore.free();
        if (presentSemaphore != null)
            presentSemaphore.free();
        renderFence.free();
        buffers.free();
        pool.free();

        if (sys.device().isSeparateGraphicsPresent()) {
            presentTransferComplete.free();
            presentFence.free();
            presentBuffers.free();
            presentPool.free();
        }
    }

}
