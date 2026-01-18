package com.vke.core.rendering.vulkan;

import com.vke.api.vulkan.LogicalDeviceCreateInfo;
import com.vke.api.vulkan.VulkanCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class LogicalDevice {

    private static final String HERE = "LogicalDevice";

    private final EngineCreateInfo engineCreateInfo;
    private final VulkanCreateInfo vulkanCreateInfo;
    private final LogicalDeviceCreateInfo logicalDeviceCreateInfo;
    private final List<VulkanQueue> queues;
    private final VKEngine engine;

    private VkDevice device;

    public LogicalDevice(VKEngine engine, LogicalDeviceCreateInfo logicalDeviceCreateInfo) {
        this.logicalDeviceCreateInfo = logicalDeviceCreateInfo;
        this.engineCreateInfo = logicalDeviceCreateInfo.engineCreateInfo;
        this.vulkanCreateInfo = engineCreateInfo.vulkanCreateInfo;
        this.queues = new ArrayList<>();
        this.engine = engine;

        initLogicalDevice(engine, vulkanCreateInfo.gpuExtensions, logicalDeviceCreateInfo.physicalDeviceWrapper);
        initQueues(logicalDeviceCreateInfo.physicalDeviceWrapper);
    }

    private void initLogicalDevice(VKEngine engine, List<String> extensions, PhysicalDevice physicalDevice) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            int requiredQueuesSize = vulkanCreateInfo.requiredQueueFamilyBitsList.size();
            PointerBuffer extBuf = VKUtils.wrapStrings(stack, extensions);

            VkDeviceQueueCreateInfo.Buffer buf = VkDeviceQueueCreateInfo.calloc(requiredQueuesSize, stack);

            int graphicsIndex = -1;
            int presentIndex = -1;
            for (int i = 0; i < physicalDevice.getQueueFamilyBuffer().capacity(); i++) {
                FloatBuffer priorities = stack.floats(0.5f);
                VkQueueFamilyProperties props = physicalDevice.getQueueFamilyBuffer().get(i);

                if (vulkanCreateInfo.requiredQueueFamilyBitsList.stream().noneMatch((bit) -> (bit & props.queueFlags()) == bit)) continue;

                if (VKUtils.bitsContains(props.queueFlags(), VK14.VK_QUEUE_GRAPHICS_BIT)) {
                    graphicsIndex = i;
                    IntBuffer output = stack.mallocInt(1);
                    KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice.getDevice(), i, logicalDeviceCreateInfo.surfaceHandle, output);
                    if (output.get(0) == 1) {
                        presentIndex = i;
                    } else {
                        continue;
                    }
                }

                

                buf.get(i).sType$Default()
                        .queueFamilyIndex(i)
                        .pQueuePriorities(priorities);
            }

            if (presentIndex == -1) engine.throwException(new IllegalStateException("Unable to find suitable graphics queue!"), HERE);

            VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.calloc(stack)
                    .sType$Default()
                    .ppEnabledExtensionNames(extBuf)
                    .pQueueCreateInfos(buf);

            PointerBuffer pLogicalDevice = stack.mallocPointer(1);
            if (VK14.vkCreateDevice(physicalDevice.getDevice(), createInfo, null, pLogicalDevice) != VK14.VK_SUCCESS) {
                engine.throwException(new RuntimeException("Failed to create Logical Device!"), HERE);
            }

            device = new VkDevice(pLogicalDevice.get(0), physicalDevice.getDevice(), createInfo);
        }
    }

    private void initQueues(PhysicalDevice physicalDevice) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            for (int i = 0; i < physicalDevice.getQueueFamilyBuffer().capacity(); i++) {
                VkQueueFamilyProperties props = physicalDevice.getQueueFamilyBuffer().get(i);

                if (vulkanCreateInfo.requiredQueueFamilyBitsList.stream().noneMatch((bit) -> (bit & props.queueFlags()) == bit)) continue;

                PointerBuffer pQueue = stack.mallocPointer(1);
                VK14.vkGetDeviceQueue(device, i, 0, pQueue);
                VkQueue queue = new VkQueue(pQueue.get(), device);
                VulkanQueue.VkQueueType type = null;

                if (props.queueFlags() == VK14.VK_QUEUE_GRAPHICS_BIT) {
                    type = VulkanQueue.VkQueueType.GRAPHICS;
                } else if (props.queueFlags() == VK14.VK_QUEUE_COMPUTE_BIT) {
                    type = VulkanQueue.VkQueueType.COMPUTE;
                }

                queues.add(new VulkanQueue(queue, i, type));
            }
        }
    }

    public long getHandle() { return this.device.address(); }

}
