package com.vke.core.rendering.vulkan.device;

import com.carrotsearch.hppc.ObjectArrayList;
import com.vke.api.event.EventBus;
import com.vke.api.logger.Logger;
import com.vke.api.rendering.abstraction.renderer.enums.QueueType;
import com.vke.core.EngineCreateInfo;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.memory.charPP;
import com.vke.core.rendering.vulkan.Consts;
import com.vke.core.rendering.vulkan.VKEvents;
import com.vke.core.rendering.vulkan.utils.VKUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class DeviceUtils {

    private static final int REQUIRED_QUEUE_FAMILIES =
                    VK14.VK_QUEUE_GRAPHICS_BIT |
                    VK14.VK_QUEUE_COMPUTE_BIT;

    public static PointerBuffer collectRequiredExtensions(AutoHeapAllocator alloc, List<String> additionalExtensions, List<String> usedExtensionsOut) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ArrayList<String> stringExtensions = new ArrayList<>(additionalExtensions);
            VKUtils.getGlfwExtensionNames(stack).forEachRemaining(stringExtensions::add);

            IntBuffer count = stack.mallocInt(1);
            VK14.vkEnumerateInstanceExtensionProperties((ByteBuffer) null, count, null);

            VkExtensionProperties.Buffer props = VkExtensionProperties.malloc(count.get(0), stack);
            VK14.vkEnumerateInstanceExtensionProperties((ByteBuffer) null, count, props);

            String missingExtension = validateRequestedExtensions(props, stringExtensions);
            if (missingExtension != null) {
                throw new IllegalStateException("Missing extension %s".formatted(missingExtension));
            }

            charPP extensionBuffer = alloc.charPP(stringExtensions.size());
            stringExtensions.forEach(ext -> {
                extensionBuffer.utf8(ext);
                if (usedExtensionsOut != null) {
                    usedExtensionsOut.add(ext);
                }
            });
            PointerBuffer pb = extensionBuffer.getHeapObject();
            pb.flip();

            return pb;
        }
    }

    public static PointerBuffer collectValidationLayers(AutoHeapAllocator alloc, List<String> usedLayerOut) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            List<String> layers = Consts.LAYERS;

            IntBuffer count = stack.mallocInt(1);
            VK14.vkEnumerateInstanceLayerProperties(count, null);

            VkLayerProperties.Buffer props = VkLayerProperties.malloc(count.get(0), stack);
            VK14.vkEnumerateInstanceLayerProperties(count, props);

            String missing = validateRequestedLayers(props, layers);

            if (missing != null) {
                throw new IllegalStateException("Missing validation layer %s!".formatted(missing));
            }

            charPP validationLayers = alloc.charPP(layers.size());
            for (String layer : layers) {
                validationLayers.utf8(layer);
                if (usedLayerOut != null) {
                    usedLayerOut.add(layer);
                }
            }
            PointerBuffer pb = validationLayers.getHeapObject();
            pb.flip();

            return pb;
        }
    }

    private static String validateRequestedExtensions(VkExtensionProperties.Buffer props, List<String> extensions) {
        outer:
        for (String ext : extensions) {
            for (VkExtensionProperties p : props) {
                String extName = p.extensionNameString();
                if (extName.equals(ext)) continue outer;
            }
            return ext;
        }

        return null;
    }

    private static String validateRequestedLayers(VkLayerProperties.Buffer props, List<String> layers) {
        outer:
        for (String layer : layers) {
            for (VkLayerProperties p : props) {
                String extName = p.layerNameString();
                if (extName.equals(layer)) continue outer;
            }
            return layer;
        }

        return null;
    }

    public static PhysicalDevice pickGpu(VkInstance instance, long surface, EventBus eventBus, Logger logger, EngineCreateInfo createInfo, List<String> extensions) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pPhysicalDeviceCount = stack.mallocInt(1);
            VK14.vkEnumeratePhysicalDevices(instance, pPhysicalDeviceCount, null);

            PointerBuffer pPhysicalDevices = stack.mallocPointer(pPhysicalDeviceCount.get(0));
            VK14.vkEnumeratePhysicalDevices(instance, pPhysicalDeviceCount, pPhysicalDevices);

            int deviceCount = pPhysicalDeviceCount.get(0);

            PhysicalDevice bestDevice = null;
            int bestScore = 0;
            for (int i = 0; i < deviceCount; i++) {
                VkPhysicalDevice device = new VkPhysicalDevice(pPhysicalDevices.get(i), instance);
                PhysicalDevice d = new PhysicalDevice(device);

                if (!meetsRequirements(d, logger, createInfo, extensions)) continue;

                int score = scoreDevice(d);
                if (score > bestScore) {
                    bestScore = score;
                    bestDevice = d;
                }

                eventBus.fire(new VKEvents.EvaluatePhysicalDevice(d, surface, score));
            }

            eventBus.fire(new VKEvents.ChosePhysicalDevice(bestDevice, bestScore));

            return bestDevice;
        }
    }

    public static boolean meetsRequirements(PhysicalDevice device, Logger logger, EngineCreateInfo createInfo, List<String> extensions) {
        VKCapabilitiesInstance c = device.getCapabilities();

        if (!createInfo.releaseMode && !c.VK_EXT_debug_utils) {
            return false;
        }
        if (c.apiVersion < 14) {
            return false;
        }

        if (!validateRequiredQueueFamilies(device)) {
            return false;
        }

        String missingExt = validateRequestedExtensions(device.getExtensionsBuffer(), extensions);
        if (missingExt != null) {
            logger.info("Couldn't select %s, because it doesn't support extension %s!", device.getName(), missingExt);
            return false;
        }

        return true;
    }

    public static int scoreDevice(PhysicalDevice device) {
        int score = 0;
        VkPhysicalDeviceProperties properties = device.getProperties();

        if (properties.deviceType() == VK14.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) {
            score += 1000;
        }

        VkPhysicalDeviceLimits limits = properties.limits();
        score += limits.maxImageDimension2D();
        score += limits.maxColorAttachments() * 10;
        score += limits.maxBoundDescriptorSets() * 10;
        score += limits.maxPushConstantsSize() * 5;

        return score;
    }

    public static boolean validateRequiredQueueFamilies(PhysicalDevice device) {
        VkQueueFamilyProperties.Buffer queueFamilies = device.getQueueFamilyBuffer();

        int a = 0;

        for (VkQueueFamilyProperties props : queueFamilies) {
            a |= props.queueFlags();
        }

        return (a & REQUIRED_QUEUE_FAMILIES) == REQUIRED_QUEUE_FAMILIES;
    }

    public static QueueType[] getQueueTypes(PhysicalDevice physicalDevice, long surfaceHandle, int i, int flags) {
        ObjectArrayList<QueueType> arr = new ObjectArrayList<>();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            QueueType[] validTypes = QueueType.validTypes();
            for (QueueType queueType : validTypes) {
                if ((flags & queueType.getIntVal()) == queueType.getIntVal()) {
                    arr.add(queueType);
                }
            }

            if (VKUtils.isPresentQueue(stack, physicalDevice, i, surfaceHandle)) {
                arr.add(QueueType.PRESENT);
            }
        }

        return arr.toArray(QueueType.class);
    }

}
