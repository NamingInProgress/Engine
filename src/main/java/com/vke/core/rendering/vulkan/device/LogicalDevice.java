package com.vke.core.rendering.vulkan.device;

import com.carrotsearch.hppc.ObjectIntHashMap;
import com.vke.core.rendering.vulkan.createInfos.LogicalDeviceCreateInfo;
import com.vke.core.rendering.vulkan.createInfos.VulkanCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.file.deflate.decompress.BitUtils;
import com.vke.core.rendering.vulkan.utils.StructureChain3;
import com.vke.core.rendering.vulkan.utils.VKUtils;
import com.vke.utils.io.Disposable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.FloatBuffer;
import java.util.*;

import com.vke.api.rendering.abstraction.renderer.enums.QueueType;

public class LogicalDevice implements Disposable {

    private static final String HERE = "LogicalDevice";

    private final EngineCreateInfo engineCreateInfo;
    private final VulkanCreateInfo vulkanCreateInfo;
    private final LogicalDeviceCreateInfo logicalDeviceCreateInfo;
    private final List<VulkanQueue> queues;
    private final VKEngine engine;

    private final Map<QueueType, QueueInfo> bestQueues = new EnumMap<>(QueueType.class);

    public boolean separateGraphicsPresentFamilies;

    private VkDevice device;

    public LogicalDevice(VKEngine engine, LogicalDeviceCreateInfo logicalDeviceCreateInfo) {
        this.logicalDeviceCreateInfo = logicalDeviceCreateInfo;
        this.engineCreateInfo = logicalDeviceCreateInfo.engineCreateInfo;
        this.vulkanCreateInfo = engineCreateInfo.vulkanCreateInfo;
        this.queues = new ArrayList<>();
        this.engine = engine;

        try(MemoryStack stack = MemoryStack.stackPush()) {
            initLogicalDevice(stack, engine, vulkanCreateInfo.gpuExtensions, logicalDeviceCreateInfo.physicalDeviceWrapper);
            initQueues(stack);
        }
    }

    private void initLogicalDevice(MemoryStack stack, VKEngine engine, List<String> extensions, PhysicalDevice physicalDevice) {
        PointerBuffer extBuf = VKUtils.wrapStrings(stack, extensions);

        Map<QueueType, Integer> bestScores = new EnumMap<>(QueueType.class);

        for (int i = 0; i < physicalDevice.getQueueFamilyBuffer().capacity(); i++) {
            VkQueueFamilyProperties props = physicalDevice.getQueueFamilyBuffer().get(i);

            int flags = props.queueFlags();
            int queueCount = props.queueCount();

            QueueType[] types = DeviceUtils.getQueueTypes(
                    physicalDevice,
                    logicalDeviceCreateInfo.surfaceHandle,
                    i,
                    flags
            );

            for (QueueType type : types) {
                int score = scoreQueueFamily(type, props, types);

                Integer bestScore = bestScores.get(type);

                    if (bestScore == null || score > bestScore || type == QueueType.PRESENT) {
                    bestScores.put(type, score);
                    bestQueues.put(type, new QueueInfo(i, types, queueCount));
                }
            }
        }

        if (!bestQueues.containsKey(QueueType.GRAPHICS)) {
            engine.throwException(new IllegalStateException("Selected device does not have a suitable graphics queue!"), HERE);
        }

        if (!bestQueues.containsKey(QueueType.PRESENT)) {
            engine.throwException(new IllegalStateException("Selected device does not have a suitable present queue!"), HERE);
        }

        Set<Integer> uniqueIndices = new HashSet<>();
        for (QueueInfo info : bestQueues.values()) {
            uniqueIndices.add(info.queueFamilyIndex);
        }

        VkDeviceQueueCreateInfo.Buffer queueCreateInfoBuffer = VkDeviceQueueCreateInfo.calloc(uniqueIndices.size(), stack);

        int counter = 0;
        for (Integer uniqueIndex : uniqueIndices) {
            FloatBuffer priorities = stack.floats(1.0f);
            queueCreateInfoBuffer.get(counter++)
                    .sType$Default()
                    .queueFamilyIndex(uniqueIndex)
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

        deviceFeaturesVK12.runtimeDescriptorArray(true);
        deviceFeaturesVK12.descriptorBindingPartiallyBound(true);
        deviceFeaturesVK12.shaderSampledImageArrayNonUniformIndexing(true);
        deviceFeaturesVK12.descriptorBindingSampledImageUpdateAfterBind(true);
        deviceFeaturesVK12.descriptorBindingUpdateUnusedWhilePending(true);


        StructureChain3<VkPhysicalDeviceVulkan12Features, VkPhysicalDeviceVulkan13Features, VkPhysicalDeviceExtendedDynamicStateFeaturesEXT> chain =
                new StructureChain3<>(deviceFeaturesVK12, deviceFeaturesVK13, deviceFeaturesEXTDynamicState,
                        deviceFeaturesVK12::pNext, deviceFeaturesVK13::pNext);

        VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.calloc(stack)
                .sType$Default()
                .pNext(chain.get().address())
                .pEnabledFeatures(deviceFeatures)
                .ppEnabledExtensionNames(extBuf)
                .pQueueCreateInfos(queueCreateInfoBuffer);

        PointerBuffer pLogicalDevice = stack.mallocPointer(1);
        if (VK14.vkCreateDevice(physicalDevice.getDevice(), createInfo, null, pLogicalDevice) != VK14.VK_SUCCESS) {
            engine.throwException(new RuntimeException("Failed to create Logical Device!"), HERE);
        }

        device = new VkDevice(pLogicalDevice.get(0), physicalDevice.getDevice(), createInfo);
    }

    private int scoreQueueFamily(QueueType requested, VkQueueFamilyProperties props, QueueType[] types) {
        int flags = props.queueFlags();

        return switch (requested) {
            case GRAPHICS -> {
                int score = 0;

                if (BitUtils.bitsContains(flags, QueueType.GRAPHICS.getIntVal()))
                    score += 1000;

                score += props.queueCount();

                yield score;
            }

            case PRESENT -> {
                int score = 0;

                if (containsPresent(types)) {
                    score += 500;

                    // Prefer a queue family that also supports graphics.
                    if (BitUtils.bitsContains(flags, QueueType.GRAPHICS.getIntVal()))
                        score += 500;
                }

                score += props.queueCount();

                yield score;
            }

            case COMPUTE -> {
                int score = 0;

                if (BitUtils.bitsContains(flags, QueueType.COMPUTE.getIntVal())) {
                    score += 500;

                    // Prefer dedicated compute.
                    if (!BitUtils.bitsContains(flags, QueueType.GRAPHICS.getIntVal()))
                        score += 500;
                }

                score += props.queueCount();

                yield score;
            }

            case TRANSFER -> {
                int score = 0;

                if (BitUtils.bitsContains(flags, QueueType.TRANSFER.getIntVal())) {
                    score += 500;

                    // Prefer dedicated transfer.
                    if (!BitUtils.bitsContains(flags, QueueType.GRAPHICS.getIntVal()) &&
                            !BitUtils.bitsContains(flags, QueueType.COMPUTE.getIntVal()))
                        score += 500;
                }

                score += props.queueCount();

                yield score;
            }
            case SPARSE -> 0;
        };
    }

    private boolean containsPresent(QueueType[] types) {
        for (QueueType t : types) {
            if (t == QueueType.PRESENT)
                return true;
        }

        return false;
    }

    private void initQueues(MemoryStack stack) {
        for (var e : bestQueues.entrySet()) {
            PointerBuffer pQueue = stack.mallocPointer(1);
            VK14.vkGetDeviceQueue(device, e.getValue().queueFamilyIndex, 0, pQueue);
            VkQueue queue = new VkQueue(pQueue.get(), device);
            QueueType type = e.getKey();

            queues.add(new VulkanQueue(queue, e.getValue().queueFamilyIndex(), type));
        }

        if (!getQueue(QueueType.PRESENT).equals(getQueue(QueueType.GRAPHICS))) {
            this.separateGraphicsPresentFamilies = true;
        }
    }

    public long getHandle() { return this.device.address(); }
    public VkDevice getDevice() { return this.device; }

    private VulkanQueue getQueueInternal(QueueType type) throws NoSuchElementException {
        for (VulkanQueue queue : queues) {
            if (queue.getType() == type) {
                return queue;
            }
        }
        throw new NoSuchElementException("Not a single queue has been found for type: " + type);
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

    public record QueueInfo(int queueFamilyIndex, QueueType[] availableTypes, int queueCount) {}

}
