package com.vke.core.rendering.vulkan.frame;

import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.device.VulkanQueue;
import com.vke.core.rendering.vulkan.commands.CommandBuffers;
import com.vke.core.rendering.vulkan.commands.CommandPool;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.rendering.vulkan.sync.Fence;
import com.vke.utils.Disposable;

public class ImmediateFrame implements Disposable {

    private CommandPool pool;
    private CommandBuffers buffers;
    private Fence fence;

    public ImmediateFrame(VKEngine engine, LogicalDevice device) {
        pool = new CommandPool(engine, device, VulkanQueue.Type.TRANSFER);
        buffers = new CommandBuffers(engine, pool, device, 1);

        setupSyncStructures(engine, device);
    }

    private void setupSyncStructures(VKEngine engine, LogicalDevice device) {

        fence = Fence.createFence(engine, device);
    }

    public CommandPool getPool() {
        return pool;
    }

    public CommandBuffers getBuffers() {
        return buffers;
    }

    public Fence getFence() {
        return fence;
    }

    @Override
    public void free() {
        fence.free();
        buffers.free();
        pool.free();
    }

}
