package com.vke.core.rendering.vulkan.command;

import com.vke.api.rendering.abstraction.DelayedDestroyer;
import com.vke.api.rendering.abstraction.renderer.commands.ImmediateSubmit;
import com.vke.api.rendering.abstraction.renderer.enums.QueueType;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import com.vke.core.rendering.vulkan.sync.VulkanFence;
import com.vke.core.rendering.vulkan.sync.VulkanSemaphore;
import com.vke.utils.io.Disposable;

import java.util.HashMap;

public class ImmediateCommandPoolManager implements Disposable {

    private final VulkanRenderSystem sys;

    private final HashMap<QueueType, CommandPool> pools = new HashMap<>();

    private final DelayedDestroyer<VulkanCmdBuffers> destroyer;

    public ImmediateCommandPoolManager(VulkanRenderSystem sys) {
        this.sys = sys;
        this.destroyer = new DelayedDestroyer<>(sys);
    }

    public ImmediateSubmit immediate(QueueType qt) {
        return immediate(qt, false);
    }

    public ImmediateSubmit immediate(QueueType qt, boolean useSignal) {
        return new ImmediateSubmit(sys.device(), qt, getBuffers(qt), new VulkanFence(sys),
                useSignal ? VulkanSemaphore.createSemaphore(sys) : null
        );
    }

    /**
     *  You shall not keep these buffers and use them for more than one frame. They WILL be automatically destroyed.
     *
     * @param type - The type of Queue that the user wants a command buffer on
     * @return - The VulkanCmdBuffers object on the specified queue type.
     */
    public VulkanCmdBuffers getBuffers(QueueType type) {
        CommandPool pool = pools.computeIfAbsent(type, (qt) -> new CommandPool(sys, qt));
        VulkanCmdBuffers buf = new VulkanCmdBuffers(sys, pool);
        destroyer.scheduleDestroy(buf);
        return buf;
    }

    @Override
    public void free() {
        destroyer.free();
        pools.values().forEach(Disposable::free);
    }

}
