package com.vke.core.rendering.vulkan;

import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.commands.CommandBuffers;
import com.vke.core.rendering.vulkan.commands.CommandPool;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.rendering.vulkan.sync.Fence;
import com.vke.core.rendering.vulkan.sync.Semaphore;
import com.vke.utils.Disposable;

public class Frame implements Disposable {

    private CommandPool pool;
    private CommandBuffers buffers;
    private Semaphore swapChainSemaphore, renderSemaphore;
    private Fence renderFence;

    public Frame(VKEngine engine, LogicalDevice device) {
        pool = new CommandPool(engine, device, VulkanQueue.Type.GRAPHICS);
        buffers = new CommandBuffers(engine, pool, device, 1);

        setupSyncStructures(engine, device);
    }

    private void setupSyncStructures(VKEngine engine, LogicalDevice device) {
        swapChainSemaphore = Semaphore.createSemaphore(engine, device);
        renderSemaphore = Semaphore.createSemaphore(engine, device);

        renderFence = Fence.createFence(engine, device);
    }

    public CommandPool getPool() {
        return pool;
    }

    public CommandBuffers getBuffers() {
        return buffers;
    }

    public Semaphore getSwapChainSemaphore() {
        return swapChainSemaphore;
    }

    public Semaphore getRenderSemaphore() {
        return renderSemaphore;
    }

    public Fence getRenderFence() {
        return renderFence;
    }

    @Override
    public void free() {
        swapChainSemaphore.free();
        renderSemaphore.free();
        renderFence.free();
        pool.free();
    }

}
