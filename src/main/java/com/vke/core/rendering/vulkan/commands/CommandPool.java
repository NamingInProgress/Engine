package com.vke.core.rendering.vulkan.commands;

import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.VulkanQueue;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.utils.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;

import java.nio.LongBuffer;

public class CommandPool implements Disposable {
    private static final String HERE = "CommandPool";

    private final long handle;
    private LogicalDevice device;

    private CommandBuffers buffers;

    public CommandPool(VKEngine engine, LogicalDevice device, VulkanQueue.Type type) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandPoolCreateInfo poolCreateInfo = VkCommandPoolCreateInfo.calloc(stack)
                    .sType$Default()
                    .flags(VK14.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)
                    .queueFamilyIndex(device.getQueue(type).index());

            LongBuffer pPool = stack.mallocLong(1);
            if (VK14.vkCreateCommandPool(device.getDevice(), poolCreateInfo, null, pPool) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create command pool for type %s".formatted(type)), HERE);
            }
            this.handle = pPool.get(0);
        }
    }

    public long getHandle() {
        return handle;
    }

    @Override
    public void free() {
        buffers.free();
        VK14.vkDestroyCommandPool(device.getDevice(), handle, null);
    }
}
