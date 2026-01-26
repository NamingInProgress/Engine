package com.vke.core.rendering.vulkan.device;

import com.carrotsearch.hppc.ObjectIntHashMap;
import com.vke.api.vulkan.createInfos.LogicalDeviceCreateInfo;
import com.vke.api.vulkan.createInfos.VulkanCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.VKUtils;
import com.vke.core.rendering.vulkan.VulkanQueue;
import com.vke.core.utils.StructureChain3;
import com.vke.utils.Disposable;
import com.vke.utils.Utils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.FloatBuffer;
import java.util.*;

import static com.vke.core.rendering.vulkan.VulkanQueue.Type;

public class LogicalDevice implements Disposable {

    private static final String HERE = "LogicalDevice";

    private final EngineCreateInfo engineCreateInfo;
    private final VulkanCreateInfo vulkanCreateInfo;
    private final LogicalDeviceCreateInfo logicalDeviceCreateInfo;
    private final List<VulkanQueue> queues;
    private final VKEngine engine;

    private final ObjectIntHashMap<Type> queueIndices;

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
                queueIndices.put(Type.GRAPHICS, i);

                if (VKUtils.isPresentQueue(stack, physicalDevice, i, logicalDeviceCreateInfo.surfaceHandle)) {
                    queueIndices.put(Type.PRESENT, i);
                }
            }
            if (VKUtils.bitsContains(flags, VK14.VK_QUEUE_COMPUTE_BIT)) {
                queueIndices.put(Type.COMPUTE, i);
            }

        }

        if (!queueIndices.containsKey(Type.PRESENT)) {
            for (int i = 0; i < physicalDevice.getQueueFamilyBuffer().capacity(); i++) {
                if (VKUtils.isPresentQueue(stack, physicalDevice, i, logicalDeviceCreateInfo.surfaceHandle)) {
                    queueIndices.put(Type.PRESENT, i);
                    break;
                }
            }
        }

        if (!queueIndices.containsKey(Type.GRAPHICS)) {
            engine.throwException(new IllegalStateException("Unable to find suitable graphics queue!"), HERE);
        }

        if (!queueIndices.containsKey(Type.PRESENT)) {
            engine.throwException(new IllegalStateException("Unable to find suitable present queue!"), HERE);
        }

        VkDeviceQueueCreateInfo.Buffer buf = VkDeviceQueueCreateInfo.calloc(
                (int) Utils.fromSpliterator(queueIndices.spliterator()).filter(c -> c.key != Type.PRESENT).count(), stack);
        int bufferIndex = 0;

        for (var entry : queueIndices) {
            if (entry.key == Type.PRESENT) {
                continue;
            }
            FloatBuffer priorities = stack.floats(0.5f);

            buf.get(bufferIndex++).sType$Default()
                    .queueFamilyIndex(entry.value)
                    .pQueuePriorities(priorities);
        }

        VkPhysicalDeviceVulkan12Features deviceFeaturesVK12 = VkPhysicalDeviceVulkan12Features.calloc(stack).sType$Default();
        VkPhysicalDeviceVulkan13Features deviceFeaturesVK13 = VkPhysicalDeviceVulkan13Features.calloc(stack).sType$Default();
        VkPhysicalDeviceExtendedDynamicStateFeaturesEXT deviceFeaturesEXTDynamicState = VkPhysicalDeviceExtendedDynamicStateFeaturesEXT.calloc(stack).sType$Default();

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
            Type type = e.key;

            queues.add(new VulkanQueue(queue, e.value, type));
        }
    }

    public long getHandle() { return this.device.address(); }
    public VkDevice getDevice() { return this.device; }

    private VulkanQueue getQueueInternal(Type type) throws NoSuchElementException {
        return this.queues.stream().filter(c -> c.getType().equals(type)).findFirst().orElseThrow();
    }

    public VulkanQueue getQueue(Type type) {
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
