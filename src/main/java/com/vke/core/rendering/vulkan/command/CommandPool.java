package com.vke.core.rendering.vulkan.command;

import com.vke.api.rendering.abstraction.renderer.enums.QueueType;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import com.vke.utils.io.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;

import java.nio.LongBuffer;

public class CommandPool implements Disposable {
    private static final String HERE = "CommandPool";

    private final long handle;
    private boolean hasBeenFreed;
    private final VulkanRenderSystem vkCtx;

    public CommandPool(VulkanRenderSystem vkCtx, QueueType type) {
        this.vkCtx = vkCtx;

        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandPoolCreateInfo poolCreateInfo = VkCommandPoolCreateInfo.calloc(stack)
                    .sType$Default()
                    .flags(VK14.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)
                    .queueFamilyIndex(vkCtx.device().getQueue(type).index());

            LongBuffer pPool = stack.mallocLong(1);
            if (VK14.vkCreateCommandPool(vkCtx.device().vkLogicalDevice(), poolCreateInfo, null, pPool) != VK14.VK_SUCCESS) {
                vkCtx.throwException(new IllegalStateException("Failed to create command pool for type %s".formatted(type)), HERE);
            }
            this.handle = pPool.get(0);
        }
    }

    public long getHandle() {
        return handle;
    }

    @Override
    public void free() {
        if (!hasBeenFreed) {
            VK14.vkDestroyCommandPool(vkCtx.device().vkLogicalDevice(), handle, null);
        }
        hasBeenFreed = true;
    }
}
