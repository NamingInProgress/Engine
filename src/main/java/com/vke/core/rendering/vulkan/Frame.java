package com.vke.core.rendering.vulkan;

import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.commands.CommandBuffers;
import com.vke.core.rendering.vulkan.commands.CommandPool;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.rendering.vulkan.sync.Fence;
import com.vke.core.rendering.vulkan.sync.Semaphore;

public class Frame {

    private CommandPool pool;
    private CommandBuffers buffers;
    private Semaphore[] semaphores;
    private Fence[] fences;

    public Frame(VKEngine engine, LogicalDevice device, int semaphoreCount, int fenceCount) {
        pool = new CommandPool(engine, device, VulkanQueue.Type.GRAPHICS);
        buffers = new CommandBuffers(engine, pool, device, 1);
        semaphores = new Semaphore[semaphoreCount];
        fences = new Fence[fenceCount];
    }

    public CommandPool getPool() {
        return pool;
    }

    public CommandBuffers getBuffers() {
        return buffers;
    }

    public Semaphore[] getSemaphores() {
        return semaphores;
    }

    public Fence[] getFences() {
        return fences;
    }

}
