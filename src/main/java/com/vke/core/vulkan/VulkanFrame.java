package com.vke.core.vulkan;

import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.commands.CommandBuffers;
import com.vke.core.rendering.vulkan.commands.CommandPool;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.rendering.vulkan.device.VulkanQueue;
import com.vke.core.vulkan.sync.VulkanFence;
import com.vke.core.vulkan.sync.VulkanSemaphore;
import com.vke.utils.Disposable;

public class VulkanFrame implements Disposable {

    private CommandPool pool;
    private CommandBuffers buffers;
    private VulkanSemaphore imageSemaphore, presentSemaphore;
    private VulkanFence renderFence;

    public VulkanFrame(VKEngine engine, LogicalDevice device) {
        pool = new CommandPool(engine, device, VulkanQueue.Type.GRAPHICS);
        buffers = new CommandBuffers(engine, pool, device, 1);

        setupSyncStructures(engine, device);
    }

    private void setupSyncStructures(VKEngine engine, LogicalDevice device) {
        try {
            imageSemaphore = VulkanSemaphore.createSemaphore(engine, device);
            presentSemaphore = VulkanSemaphore.createSemaphore(engine, device);

            renderFence = new VulkanFence(device);
        } catch (Throwable t) {
            engine.throwException(t, "VulkanFrame");
        }
    }

    public CommandPool getPool() {
        return pool;
    }

    public CommandBuffers getBuffers() {
        return buffers;
    }

    public VulkanSemaphore getImageSemaphore() {
        return imageSemaphore;
    }

    public VulkanSemaphore getPresentSemaphore() {
        return presentSemaphore;
    }

    public VulkanFence getRenderFence() {
        return renderFence;
    }

    @Override
    public void free() {
        imageSemaphore.free();
        presentSemaphore.free();
        renderFence.free();
        buffers.free();
        pool.free();
    }

}
