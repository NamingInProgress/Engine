package com.vke.core.rendering.vulkan;

import com.carrotsearch.hppc.IntArrayList;
import com.vke.api.logger.LogLevel;
import com.vke.api.logger.Logger;
import com.vke.api.vulkan.LogicalDeviceCreateInfo;
import com.vke.api.vulkan.VulkanCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.logger.LoggerFactory;
import com.vke.utils.Pair;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

public class VulkanSetup {

    private static final String HERE = "Vulkan Init";

    private static final VkDebugUtilsMessengerCallbackEXTI debugMessengerCallback = (severity, type, pCallbackData, pUserData) -> {
        VkDebugUtilsMessengerCallbackDataEXT data = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);
        LoggerFactory.get("VK-Debug").log(LogLevel.fromVkMessageSeverity(severity), "%s: %s".formatted(Utils.getDebugMessageType(type), data.pMessageString()));
        data.close();
        return VK14.VK_FALSE;
    };

    private final EngineCreateInfo engineCreateInfo;
    private final VulkanCreateInfo vulkanCreateInfo;
    private VkInstance instance;
    private long surface, debugMessenger;

    public VulkanSetup(EngineCreateInfo engineCreateInfo) {
        this.engineCreateInfo = engineCreateInfo;
        this.vulkanCreateInfo = engineCreateInfo.vulkanCreateInfo;
    }

    @SuppressWarnings("all")
    public void initVulkan(VKEngine engine) {
        if (!engineCreateInfo.releaseMode) vulkanCreateInfo.extensions.add(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME);

        ArrayList<String> usedExtensions = new ArrayList<>();
        ArrayList<String> usedLayers = new ArrayList<>();

        PointerBuffer validationLayers = collectValidationLayers(engine, usedLayers);
        PointerBuffer extensions = collectRequiredExtensions(engine, usedExtensions);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack)
                    .sType(VK14.VK_STRUCTURE_TYPE_APPLICATION_INFO)
                    .pApplicationName(stack.UTF8(engineCreateInfo.applicationName))
                    .applicationVersion(engineCreateInfo.applicationVersion.getVkFormatVersion())
                    .pEngineName(stack.UTF8(engineCreateInfo.engine))
                    .engineVersion(engineCreateInfo.engineVersion.getVkFormatVersion())
                    .apiVersion(vulkanCreateInfo.apiVersion.getVkFormatVersion());

            VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.calloc(stack)
                    .sType(VK14.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                    .pApplicationInfo(appInfo)
                    .ppEnabledLayerNames(validationLayers)
                    .ppEnabledExtensionNames(extensions);

            /**  Instance Creation  **/
            PointerBuffer pInstance = stack.mallocPointer(1);

            if (VK14.vkCreateInstance(createInfo, null, pInstance) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("VkInstance couldn't be created"), HERE);
            }

            instance = new VkInstance(pInstance.get(0), createInfo);

            setupDebugMessenger(instance, engine);

            /**  Surface Creation  **/
            LongBuffer pSurface = stack.mallocLong(1);
            GLFWVulkan.glfwCreateWindowSurface(instance, engine.getWindow().getHandle(), null, pSurface);
            surface = pSurface.get(0);

            /**  Device Setup  **/
            Pair<VkPhysicalDevice, IntArrayList> physicalDeviceWrapper = pickGpu(engine, engineCreateInfo, vulkanCreateInfo.gpuExtensions);
            String name = Utils.getGpuName(stack, physicalDeviceWrapper.v1);
            engine.getLogger().info("Using GPU: " + name);

            LogicalDeviceCreateInfo deviceCreateInfo = new LogicalDeviceCreateInfo();
            deviceCreateInfo.physicalDevice = physicalDeviceWrapper.v1;
            deviceCreateInfo.queueIndices = physicalDeviceWrapper.v2.toArray();
            deviceCreateInfo.engineCreateInfo = engineCreateInfo;
            LogicalDevice device = new LogicalDevice(engine, deviceCreateInfo);
        }
    }

    public void cleanUp() {
        KHRSurface.vkDestroySurfaceKHR(instance, surface, null);
        EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, null);
        VK14.vkDestroyInstance(instance, null);
    }

    private void setupDebugMessenger(VkInstance instance, VKEngine engine) {
        if (engineCreateInfo.releaseMode) return;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDebugUtilsMessengerCreateInfoEXT debugMessengerCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc()
                    .sType$Default()
                    .messageSeverity(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT |
                            EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT |
                            EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT |
                            EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT)
                    .messageType(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT |
                            EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT |
                            EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT)
                    .pfnUserCallback(debugMessengerCallback);

            LongBuffer pMessenger = stack.mallocLong(1);

            if (EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(instance, debugMessengerCreateInfo, null, pMessenger) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Debug Messenger couldn't be created!"), HERE);
            }
            debugMessenger = pMessenger.get(0);
        }
    }

    private Pair<VkPhysicalDevice, IntArrayList> pickGpu(VKEngine engine, EngineCreateInfo createInfo, List<String> extensions) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pPhysicalDeviceCount = stack.mallocInt(1);
            VK14.vkEnumeratePhysicalDevices(instance, pPhysicalDeviceCount, null);

            PointerBuffer pPhysicalDevices = stack.mallocPointer(pPhysicalDeviceCount.get(0));
            VK14.vkEnumeratePhysicalDevices(instance, pPhysicalDeviceCount, pPhysicalDevices);

            int deviceCount = pPhysicalDeviceCount.get(0);

            VkPhysicalDevice bestDevice = null;
            int bestScore = 0;
            for (int i = 0; i < deviceCount; i++) {
                VkPhysicalDevice device = new VkPhysicalDevice(pPhysicalDevices.get(i), instance);
                if (!meetsRequirements(device, engine, createInfo, extensions)) continue;

                int score = scoreDevice(stack, device);
                if (score > bestScore) {
                    bestScore = score;
                    bestDevice = device;
                }
            }

            return new Pair<>(bestDevice, getRequiredQueueFamilyIndices(bestDevice, vulkanCreateInfo.requiredQueueFamilyBits));
        }
    }

    private boolean meetsRequirements(VkPhysicalDevice device, VKEngine engine, EngineCreateInfo createInfo, List<String> extensions) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VKCapabilitiesInstance c = device.getCapabilities();

            if (!createInfo.releaseMode && !c.VK_EXT_debug_utils) {
                return false;
            }
            if (c.apiVersion < 14) {
                return false;
            }

            if (getRequiredQueueFamilyIndices(device, vulkanCreateInfo.requiredQueueFamilyBits) == null) {
                return false;
            }

            IntBuffer pExtCount = stack.mallocInt(1);
            VK14.vkEnumerateDeviceExtensionProperties(device, (ByteBuffer) null, pExtCount, null);

            VkExtensionProperties.Buffer extBuffer = VkExtensionProperties.malloc(pExtCount.get(0), stack);
            VK14.vkEnumerateDeviceExtensionProperties(device, (ByteBuffer) null, pExtCount, extBuffer);

            String missingExt = validateRequestedExtensions(extBuffer, extensions);
            if (missingExt != null) {
                String deviceName = Utils.getGpuName(stack, device);
                Logger logger = engine.getLogger();
                logger.info("Couldn't select %s, because it doesn't support extension %s!", deviceName, missingExt);
                return false;
            }

            return true;
        }
    }

    private int scoreDevice(MemoryStack stack, VkPhysicalDevice device) {
        int score = 0;
        VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.calloc(stack);
        VK14.vkGetPhysicalDeviceProperties(device, properties);

        if (properties.deviceType() == VK14.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) {
            score += 1000;
        }

        VkPhysicalDeviceLimits limits = properties.limits();
        score += limits.maxImageDimension2D();
        score += limits.maxColorAttachments() * 10;
        score += limits.maxBoundDescriptorSets() * 10;

        return score;
    }

    private PointerBuffer collectRequiredExtensions(VKEngine engine, List<String> usedExtensionsOut) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ArrayList<String> stringExtensions = new ArrayList<>(vulkanCreateInfo.extensions);
            Utils.getGlfwExtensionNames(stack).forEachRemaining(stringExtensions::add);

            IntBuffer count = stack.mallocInt(1);
            VK14.vkEnumerateInstanceExtensionProperties((ByteBuffer) null, count, null);

            VkExtensionProperties.Buffer props = VkExtensionProperties.malloc(count.get(0), stack);
            VK14.vkEnumerateInstanceExtensionProperties((ByteBuffer) null, count, props);

            String missingExtension = validateRequestedExtensions(props, stringExtensions);
            if (missingExtension != null) {
                engine.throwException(new IllegalStateException("Missing extension %s".formatted(missingExtension)), HERE);
            }

            PointerBuffer extensionBuffer = stack.mallocPointer(stringExtensions.size());
            stringExtensions.forEach(ext -> {
                extensionBuffer.put(stack.UTF8(ext));
                if (usedExtensionsOut != null) {
                    usedExtensionsOut.add(ext);
                }
            });
            extensionBuffer.flip();

            return extensionBuffer;
        }
    }

    private PointerBuffer collectValidationLayers(VKEngine engine, List<String> usedLayerOut) {
        if (!engineCreateInfo.releaseMode) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                List<String> layers = Consts.LAYERS;

                IntBuffer count = stack.mallocInt(1);
                VK14.vkEnumerateInstanceLayerProperties(count, null);

                VkLayerProperties.Buffer props = VkLayerProperties.malloc(count.get(0), stack);
                VK14.vkEnumerateInstanceLayerProperties(count, props);

                String missing = validateRequestedLayers(props, layers);

                if (missing != null) {
                    engine.throwException(new RuntimeException("Missing validation layer %s!".formatted(missing)), HERE);
                }

                PointerBuffer validationLayers = stack.mallocPointer(layers.size());
                for (String layer : layers) {
                    validationLayers.put(stack.UTF8(layer));
                    if (usedLayerOut != null) {
                        usedLayerOut.add(layer);
                    }
                }
                validationLayers.flip();

                return validationLayers;
            }
        }

        return null;
    }

    private String validateRequestedExtensions(VkExtensionProperties.Buffer props, List<String> extensions) {
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

    private IntArrayList getRequiredQueueFamilyIndices(VkPhysicalDevice device, int requireQueueFlagBits) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntArrayList queueIndices = new IntArrayList();

            IntBuffer count = stack.mallocInt(1);
            VK14.vkGetPhysicalDeviceQueueFamilyProperties(device, count, null);

            VkQueueFamilyProperties.Buffer props = VkQueueFamilyProperties.malloc(count.get(0), stack);
            VK14.vkGetPhysicalDeviceQueueFamilyProperties(device, count, props);

            int a = 0;

            for (int i = 0; i < count.get(0); i++) {
                var queueFamily = props.get(i);
                a |= queueFamily.queueFlags();

                if ((queueFamily.queueFlags() & requireQueueFlagBits) != 0) queueIndices.add(i);
            }

            if ((a & requireQueueFlagBits) == requireQueueFlagBits) return queueIndices;
        }

        return null;
    }

    private String validateRequestedLayers(VkLayerProperties.Buffer props, List<String> layers) {
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

}
