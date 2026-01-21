package com.vke.core.rendering.vulkan;

import com.carrotsearch.hppc.ObjectIntHashMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.vke.api.vulkan.LogicalDeviceCreateInfo;
import com.vke.api.vulkan.VulkanCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.utils.Disposable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.FloatBuffer;
import java.util.*;

import static com.vke.core.rendering.vulkan.VulkanQueue.VkQueueType;

public class LogicalDevice implements Disposable {

    private static final String HERE = "LogicalDevice";

    private final EngineCreateInfo engineCreateInfo;
    private final VulkanCreateInfo vulkanCreateInfo;
    private final LogicalDeviceCreateInfo logicalDeviceCreateInfo;
    private final List<VulkanQueue> queues;
    private final VKEngine engine;

    private final ObjectIntHashMap<VkQueueType> queueIndices;

    private VkDevice device;

    public LogicalDevice(VKEngine engine, LogicalDeviceCreateInfo logicalDeviceCreateInfo) {
        this.logicalDeviceCreateInfo = logicalDeviceCreateInfo;
        this.engineCreateInfo = logicalDeviceCreateInfo.engineCreateInfo;
        this.vulkanCreateInfo = engineCreateInfo.vulkanCreateInfo;
        this.queues = new ArrayList<>();
        this.engine = engine;

        queueIndices = new ObjectIntHashMap<>();

        try(MemoryStack stack = MemoryStack.stackPush()) {
            initLogicalDevice(stack, engine, vulkanCreateInfo.gpuExtensions, logicalDeviceCreateInfo.physicalDeviceWrapper);
            initQueues(stack);
        }
    }

    private void initLogicalDevice(MemoryStack stack, VKEngine engine, List<String> extensions, PhysicalDevice physicalDevice) {
        PointerBuffer extBuf = VKUtils.wrapStrings(stack, extensions);

        queueIndices.clear();
        for (int i = 0; i < physicalDevice.getQueueFamilyBuffer().capacity(); i++) {
            VkQueueFamilyProperties props = physicalDevice.getQueueFamilyBuffer().get(i);
            int flags = props.queueFlags();
            if (VKUtils.bitsContains(flags, VK14.VK_QUEUE_GRAPHICS_BIT)) {
                queueIndices.put(VkQueueType.GRAPHICS, i);

                if (VKUtils.isPresentQueue(stack, physicalDevice, i, logicalDeviceCreateInfo.surfaceHandle)) {
                    queueIndices.put(VkQueueType.PRESENT, i);
                }
            }
            if (VKUtils.bitsContains(flags, VK14.VK_QUEUE_COMPUTE_BIT)) {
                queueIndices.put(VkQueueType.COMPUTE, i);
            }

        }

        if (!queueIndices.containsKey(VkQueueType.PRESENT)) {
            for (int i = 0; i < physicalDevice.getQueueFamilyBuffer().capacity(); i++) {
                if (VKUtils.isPresentQueue(stack, physicalDevice, i, logicalDeviceCreateInfo.surfaceHandle)) {
                    queueIndices.put(VkQueueType.PRESENT, i);
                    break;
                }
            }
        }

        if (!queueIndices.containsKey(VkQueueType.GRAPHICS)) {
            engine.throwException(new IllegalStateException("Unable to find suitable graphics queue!"), HERE);
        }

        if (!queueIndices.containsKey(VkQueueType.PRESENT)) {
            engine.throwException(new IllegalStateException("Unable to find suitable present queue!"), HERE);
        }

        VkDeviceQueueCreateInfo.Buffer buf = VkDeviceQueueCreateInfo.calloc((int) Arrays.stream(queueIndices.keys).filter(c -> c != VkQueueType.PRESENT && c != null).count(), stack);
        int bufferIndex = 0;

        for (var entry : queueIndices) {
            if (entry.key == VkQueueType.PRESENT) {
                continue;
            }
            FloatBuffer priorities = stack.floats(0.5f);

            buf.get(bufferIndex++).sType$Default()
                    .queueFamilyIndex(entry.value)
                    .pQueuePriorities(priorities);
        }

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

    private void initQueues(MemoryStack stack) {
        for (var e : queueIndices) {
            PointerBuffer pQueue = stack.mallocPointer(1);
            VK14.vkGetDeviceQueue(device, e.value, 0, pQueue);
            VkQueue queue = new VkQueue(pQueue.get(), device);
            VulkanQueue.VkQueueType type = e.key;

            queues.add(new VulkanQueue(queue, e.value, type));
        }
    }

    public long getHandle() { return this.device.address(); }
    public VkDevice getDevice() { return this.device; }

    private VulkanQueue getQueueInternal(VkQueueType type) throws NoSuchElementException {
        return this.queues.stream().filter(c -> c.getType().equals(type)).findFirst().orElseThrow();
    }

    public VulkanQueue getQueue(VkQueueType type) {
        try {
            return getQueueInternal(type);
        } catch (NoSuchElementException e) {
            engine.throwException(new IllegalStateException("No queue available for type " + type), HERE);
        }
        return null;
    }

    @Override
    public void free() {
        VK14.vkDestroyDevice(device, null);
    }
}
