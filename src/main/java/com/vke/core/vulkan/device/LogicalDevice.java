package com.vke.core.vulkan.device;

import com.carrotsearch.hppc.ObjectIntHashMap;
import com.vke.core.vulkan.createInfos.LogicalDeviceCreateInfo;
import com.vke.core.vulkan.createInfos.VulkanCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.file.deflate.decompress.BitUtils;
import com.vke.core.vulkan.VKUtils;
import com.vke.core.vulkan.utils.StructureChain3;
import com.vke.utils.io.Disposable;
import com.vke.utils.Utils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.FloatBuffer;
import java.util.*;

import com.vke.api.rendering.abstraction.enums.QueueType;

public class LogicalDevice implements Disposable {

    private static final String HERE = "LogicalDevice";

    private final EngineCreateInfo engineCreateInfo;
    private final VulkanCreateInfo vulkanCreateInfo;
    private final LogicalDeviceCreateInfo logicalDeviceCreateInfo;
    private final List<VulkanQueue> queues;
    private final VKEngine engine;

    private final ObjectIntHashMap<QueueType> queueIndices;

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
            if (BitUtils.bitsContains(flags, VK14.VK_QUEUE_GRAPHICS_BIT)) {
                queueIndices.put(QueueType.GRAPHICS, i);

                if (VKUtils.isPresentQueue(stack, physicalDevice, i, logicalDeviceCreateInfo.surfaceHandle)) {
                    queueIndices.put(QueueType.PRESENT, i);
                }
            }
            if (BitUtils.bitsContains(flags, VK14.VK_QUEUE_COMPUTE_BIT)) {
                queueIndices.put(QueueType.COMPUTE, i);
            }
            if (BitUtils.bitsContains(flags, VK14.VK_QUEUE_TRANSFER_BIT)) {
                queueIndices.put(QueueType.TRANSFER, i);
            }
        }

        if (!queueIndices.containsKey(QueueType.PRESENT)) {
            for (int i = 0; i < physicalDevice.getQueueFamilyBuffer().capacity(); i++) {
                if (VKUtils.isPresentQueue(stack, physicalDevice, i, logicalDeviceCreateInfo.surfaceHandle)) {
                    queueIndices.put(QueueType.PRESENT, i);
                    break;
                }
            }
        }

        if (!queueIndices.containsKey(QueueType.GRAPHICS)) {
            engine.throwException(new IllegalStateException("Unable to find suitable graphics queue!"), HERE);
        }

        if (!queueIndices.containsKey(QueueType.PRESENT)) {
            engine.throwException(new IllegalStateException("Unable to find suitable present queue!"), HERE);
        }

        VkDeviceQueueCreateInfo.Buffer buf = VkDeviceQueueCreateInfo.calloc(
                (int) Utils.fromSpliterator(queueIndices.spliterator()).filter(c -> c.key != QueueType.PRESENT).count(), stack);
        int bufferIndex = 0;

        for (var entry : queueIndices) {
            if (entry.key == QueueType.PRESENT) {
                continue;
            }
            FloatBuffer priorities = stack.floats(0.5f);

            buf.get(bufferIndex++).sType$Default()
                    .queueFamilyIndex(entry.value)
                    .pQueuePriorities(priorities);
        }

        VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc(stack);
        VkPhysicalDeviceVulkan12Features deviceFeaturesVK12 = VkPhysicalDeviceVulkan12Features.calloc(stack).sType$Default();
        VkPhysicalDeviceVulkan13Features deviceFeaturesVK13 = VkPhysicalDeviceVulkan13Features.calloc(stack).sType$Default();
        VkPhysicalDeviceExtendedDynamicStateFeaturesEXT deviceFeaturesEXTDynamicState = VkPhysicalDeviceExtendedDynamicStateFeaturesEXT.calloc(stack).sType$Default();

        deviceFeatures.fillModeNonSolid(true);
        deviceFeatures.wideLines(true);

        deviceFeaturesVK12.bufferDeviceAddress(true);
        deviceFeaturesVK12.descriptorIndexing(true);
        deviceFeaturesVK13.synchronization2(true);
        deviceFeaturesVK13.dynamicRendering(true);
        deviceFeaturesEXTDynamicState.extendedDynamicState(true);



        StructureChain3<VkPhysicalDeviceVulkan12Features, VkPhysicalDeviceVulkan13Features, VkPhysicalDeviceExtendedDynamicStateFeaturesEXT> chain =
                new StructureChain3<>(deviceFeaturesVK12, deviceFeaturesVK13, deviceFeaturesEXTDynamicState, deviceFeaturesVK12::pNext, deviceFeaturesVK13::pNext);

        VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.calloc(stack)
                .sType$Default()
                .pNext(chain.get().address())
                .pEnabledFeatures(deviceFeatures)
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
            QueueType type = e.key;

            queues.add(new VulkanQueue(queue, e.value, type));
        }
    }

    public long getHandle() { return this.device.address(); }
    public VkDevice getDevice() { return this.device; }

    private VulkanQueue getQueueInternal(QueueType type) throws NoSuchElementException {
        return this.queues.stream().filter(c -> c.getType().equals(type)).findFirst().orElseThrow();
    }

    public VulkanQueue getQueue(QueueType type) {
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

    public PhysicalDevice getPhysicalDevice() {
        return this.logicalDeviceCreateInfo.physicalDeviceWrapper;
    }
}
