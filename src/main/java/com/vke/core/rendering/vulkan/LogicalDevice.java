package com.vke.core.rendering.vulkan;

import com.vke.api.vulkan.LogicalDeviceCreateInfo;
import com.vke.api.vulkan.VulkanCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.FloatBuffer;
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

        initLogicalDevice(engine, vulkanCreateInfo.gpuExtensions, logicalDeviceCreateInfo.queueIndices, logicalDeviceCreateInfo.physicalDevice);
        initQueues(logicalDeviceCreateInfo.queueIndices);
    }

    private void initLogicalDevice(VKEngine engine, List<String> extensions, int[] queueFamilies, VkPhysicalDevice physicalDevice) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer extBuf = Utils.wrapStrings(stack, extensions);

            VkDeviceQueueCreateInfo.Buffer buf = VkDeviceQueueCreateInfo.calloc(queueFamilies.length, stack);
            for (int i = 0; i < queueFamilies.length; i++) {
                int familyIndex = queueFamilies[i];
                FloatBuffer priorities = stack.floats(0.5f);

                buf.get(i)
                        .sType$Default()
                        .queueFamilyIndex(familyIndex)
                        .pQueuePriorities(priorities);
            }

            VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.calloc(stack)
                    .sType$Default()
                    .ppEnabledExtensionNames(extBuf)
                    .pQueueCreateInfos(buf);

            PointerBuffer pLogicalDevice = stack.mallocPointer(1);
            if (VK14.vkCreateDevice(physicalDevice, createInfo, null, pLogicalDevice) != VK14.VK_SUCCESS) {
                engine.throwException(new RuntimeException("Failed to create Logical Device!"), HERE);
            }

            device = new VkDevice(pLogicalDevice.get(0), physicalDevice, createInfo);
        }
    }

    private void initQueues(int[] queueIndices) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            for (int familyIndex : queueIndices) {
                int queueIndex = 0;

                PointerBuffer pQueue = stack.mallocPointer(1);
                VK14.vkGetDeviceQueue(device, familyIndex, queueIndex, pQueue);
                VkQueue queue = new VkQueue(pQueue.get(), device);

                queues.add(new VulkanQueue(queue, familyIndex));
            }
        }
    }

    public long getHandle() { return this.device.address(); }

}
