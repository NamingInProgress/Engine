package com.vke.core.rendering.vulkan.command;

import com.vke.api.rendering.abstraction.renderer.commands.ImmediateSubmit;
import com.vke.api.rendering.abstraction.renderer.enums.QueueType;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import com.vke.core.rendering.vulkan.sync.VulkanFence;
import com.vke.core.rendering.vulkan.sync.VulkanSemaphore;
import com.vke.utils.io.Disposable;

import java.util.EnumMap;
import java.util.Map;

public class ImmediateCommandPoolManager implements Disposable {

    private final VulkanRenderSystem sys;

    private final Map<QueueType, CommandPool> pools = new EnumMap<>(QueueType.class);

    public ImmediateCommandPoolManager(VulkanRenderSystem sys) {
        this.sys = sys;
    }

    public ImmediateSubmit immediate(QueueType qt) {
        return immediate(qt, false);
    }

    public ImmediateSubmit immediate(QueueType qt, boolean useSignal) {
        return immediate(qt, useSignal, true);
    }

    public ImmediateSubmit immediate(QueueType qt, boolean useSignal, boolean useFence) {
        return new ImmediateSubmit(sys.device(), qt, getBuffers(qt), useFence ? new VulkanFence(sys) : null,
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
        return new VulkanCmdBuffers(sys, pool);
    }

    @Override
    public void free() {
        pools.values().forEach(Disposable::free);
    }

}
