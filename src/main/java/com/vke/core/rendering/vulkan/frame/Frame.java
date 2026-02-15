package com.vke.core.rendering.vulkan.frame;

import com.vke.core.VKEngine;
import com.vke.api.abstraction.descriptors.QueueType;
import com.vke.core.rendering.vulkan.commands.CommandBuffers;
import com.vke.core.vulkan.command.CommandPool;
import com.vke.core.vulkan.device.LogicalDevice;
import com.vke.core.rendering.vulkan.sync.Fence;
import com.vke.core.vulkan.sync.VulkanSemaphore;
import com.vke.utils.Disposable;

public class Frame implements Disposable {

    private CommandPool pool;
    private CommandBuffers buffers;
    private VulkanSemaphore swapChainSemaphore, renderSemaphore;
    private Fence renderFence;

    public Frame(VKEngine engine, LogicalDevice device) {
        pool = new CommandPool(engine, device, QueueType.GRAPHICS);
        buffers = new CommandBuffers(engine, pool, device, 1);

        setupSyncStructures(engine, device);
    }

    private void setupSyncStructures(VKEngine engine, LogicalDevice device) {
        swapChainSemaphore = VulkanSemaphore.createSemaphore(engine, device);
        renderSemaphore = VulkanSemaphore.createSemaphore(engine, device);

        renderFence = Fence.createFence(engine, device);
    }

    public CommandPool getPool() {
        return pool;
    }

    public CommandBuffers getBuffers() {
        return buffers;
    }

    public VulkanSemaphore getSwapChainSemaphore() {
        return swapChainSemaphore;
    }

    public VulkanSemaphore getRenderSemaphore() {
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
        buffers.free();
        pool.free();
    }

}
