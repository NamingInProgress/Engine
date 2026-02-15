package com.vke.core.vulkan.device;

import com.vke.api.abstraction.descriptors.QueueType;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.VulkanSetup;
import com.vke.core.rendering.vulkan.commands.CommandBuffers;
import com.vke.core.rendering.vulkan.frame.Frame;
import com.vke.core.rendering.vulkan.frame.ImmediateFrame;
import com.vke.core.vulkan.sync.VulkanSemaphore;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.util.Objects;

public class VulkanQueue {
    private final VkQueue queue;
    private final int familyIndex;
    private final QueueType queueType;

    public VulkanQueue(VkQueue queue, int familyIndex, QueueType queueType) {
        Objects.requireNonNull(queueType, "Queue type must not be null!");
        this.queue = queue;
        this.familyIndex = familyIndex;
        this.queueType = queueType;
    }

    public QueueType getType() { return this.queueType; }
    public int index() { return this.familyIndex; }
    public VkQueue vk() { return queue; }

    public void submit(VKEngine engine, MemoryStack stack, VulkanSetup setup, Frame frame, int frameCount) {
        //submit queue
        VkCommandBufferSubmitInfo cmdSubmitInfo = CommandBuffers.getDefaultSubmitInfo(stack, frame.getBuffers());

        VkSemaphoreSubmitInfo waitInfo = VulkanSemaphore.getDefaultSubmitInfo(stack, frame.getSwapChainSemaphore(), (int) VK14.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT);
        VkSemaphoreSubmitInfo signalInfo = VulkanSemaphore.getDefaultSubmitInfo(stack, frame.getRenderSemaphore(), (int) VK14.VK_PIPELINE_STAGE_2_ALL_GRAPHICS_BIT);

        VkSubmitInfo2 submitInfo = createSubmitInfo2(stack, cmdSubmitInfo, waitInfo, signalInfo);
        VkSubmitInfo2.Buffer submitBuf = VkSubmitInfo2.calloc(1, stack);
        submitBuf.put(0, submitInfo);

        VkQueue graphicsQueue = setup.getLogicalDevice().getQueue(QueueType.GRAPHICS).vk();
        if (VK14.vkQueueSubmit2(graphicsQueue, submitBuf, frame.getRenderFence().getHandle()) != VK14.VK_SUCCESS) {
            engine.getLogger().warn("Failed to submit queue at frame " + frameCount);
        }
    }

    public void submitImmediate(VKEngine engine, MemoryStack stack, ImmediateFrame frame) {
        //submit queue
        VkCommandBufferSubmitInfo cmdSubmitInfo = CommandBuffers.getDefaultSubmitInfo(stack, frame.getBuffers());

        VkSubmitInfo2 submitInfo = createSubmitInfo2_immeditate(stack, cmdSubmitInfo);
        VkSubmitInfo2.Buffer submitBuf = VkSubmitInfo2.calloc(1, stack);
        submitBuf.put(0, submitInfo);

        VkQueue transferQueue = vk();
        if (VK14.vkQueueSubmit2(transferQueue, submitBuf, frame.getFence().getHandle()) != VK14.VK_SUCCESS) {
            engine.getLogger().warn("Failed to submit immediate queue");
        }
    }

    private VkSubmitInfo2 createSubmitInfo2(MemoryStack stack, VkCommandBufferSubmitInfo cmdInfo, VkSemaphoreSubmitInfo wait, VkSemaphoreSubmitInfo signal) {
        VkCommandBufferSubmitInfo.Buffer cmdBuf = VkCommandBufferSubmitInfo.calloc(1, stack);
        cmdBuf.put(0, cmdInfo);

        VkSemaphoreSubmitInfo.Buffer waitBuf = VkSemaphoreSubmitInfo.calloc(1, stack);
        waitBuf.put(0, wait);

        VkSemaphoreSubmitInfo.Buffer signalBuf = VkSemaphoreSubmitInfo.calloc(1, stack);
        signalBuf.put(0, signal);

        VkSubmitInfo2 info = VkSubmitInfo2.calloc(stack);
        info.sType$Default();
        info.pCommandBufferInfos(cmdBuf);
        info.pWaitSemaphoreInfos(waitBuf);
        info.pSignalSemaphoreInfos(signalBuf);

        return info;
    }

    private VkSubmitInfo2 createSubmitInfo2_immeditate(MemoryStack stack, VkCommandBufferSubmitInfo cmdInfo) {
        VkCommandBufferSubmitInfo.Buffer cmdBuf = VkCommandBufferSubmitInfo.calloc(1, stack);
        cmdBuf.put(0, cmdInfo);

        VkSubmitInfo2 info = VkSubmitInfo2.calloc(stack);
        info.sType$Default();
        info.pCommandBufferInfos(cmdBuf);
        info.pWaitSemaphoreInfos(null);
        info.pSignalSemaphoreInfos(null);

        return info;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof VulkanQueue )) return false;
        return familyIndex == ((VulkanQueue) other).familyIndex;
    }

}
