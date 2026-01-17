package com.vke.core.rendering.vulkan;

import com.vke.api.logger.LogLevel;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.logger.LoggerFactory;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

public class VulkanSetup {
    private static final String HERE = "Vulkan Init";
    public static final VkDebugUtilsMessengerCallbackEXTI debugMessengerCallback = (severity, type, pCallbackData, pUserData) -> {
        VkDebugUtilsMessengerCallbackDataEXT data = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);
        LoggerFactory.get("VK-Debug").log(LogLevel.fromVkMessageSeverity(severity), "%s: %s".formatted(Utils.getDebugMessageType(type), data.pMessageString()));
        data.close();
        return VK14.VK_FALSE;
    };

    private final EngineCreateInfo engineCreateInfo;
    private VkInstance instance;
    private long surface, debugMessenger;

    public VulkanSetup(EngineCreateInfo engineCreateInfo) {
        this.engineCreateInfo = engineCreateInfo;
    }

    public void initVulkan(VKEngine engine) {
        if (!engineCreateInfo.releaseMode) engineCreateInfo.vkExtensions.add("VK_EXT_debug_utils");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack)
                    .sType(VK14.VK_STRUCTURE_TYPE_APPLICATION_INFO)
                    .pApplicationName(stack.UTF8(engineCreateInfo.applicationName))
                    .applicationVersion(engineCreateInfo.applicationVersion.getVkFormatVersion())
                    .pEngineName(stack.UTF8(engineCreateInfo.engine))
                    .engineVersion(engineCreateInfo.engineVersion.getVkFormatVersion())
                    .apiVersion(engineCreateInfo.vkAPIVersion.getVkFormatVersion());

            ArrayList<ByteBuffer> extensions = enumerateExtensions(engine, stack);

            PointerBuffer validationLayer = null;
            if (!engineCreateInfo.releaseMode) {
                validationLayer = stack.mallocPointer(1);
                validationLayer.put(stack.UTF8("VK_LAYER_KHRONOS_validation"));
                validationLayer.flip();
            }

            VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.calloc(stack)
                    .sType(VK14.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                    .pApplicationInfo(appInfo)
                    .ppEnabledLayerNames(validationLayer)
                    .ppEnabledExtensionNames(Utils.wrap(stack, extensions));
            System.out.println(createInfo.enabledLayerCount());

            PointerBuffer pInstance = stack.mallocPointer(1);
            if (VK14.vkCreateInstance(createInfo, null, pInstance) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("VkInstance couldn't be created"), HERE);
            }
            instance = new VkInstance(pInstance.get(0), createInfo);

            setupDebugMessenger(instance, engine, stack);

            LongBuffer pSurface = stack.mallocLong(1);
            GLFWVulkan.glfwCreateWindowSurface(instance, engine.getWindow().getHandle(), null, pSurface);
            surface = pSurface.get(0);
        }
    }

    public void cleanUp() {
        KHRSurface.vkDestroySurfaceKHR(instance, surface, null);
        EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, null);
        VK14.vkDestroyInstance(instance, null);
    }

    private void setupDebugMessenger(VkInstance instance, VKEngine engine, MemoryStack stack) {
        if (engineCreateInfo.releaseMode) return;

        VkDebugUtilsMessengerCreateInfoEXT debugMessengerCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack)
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

    private VkPhysicalDevice pickGpu(MemoryStack stack, VKEngine engine, EngineCreateInfo createInfo) {
        VkPhysicalDeviceVulkan14Features features14 = VkPhysicalDeviceVulkan14Features.calloc(stack);
        VkPhysicalDeviceVulkan13Features features13 = VkPhysicalDeviceVulkan13Features.calloc(stack);
        features13.dynamicRendering(true);
        features13.synchronization2(true);

        VkPhysicalDeviceVulkan12Features features12 = VkPhysicalDeviceVulkan12Features.calloc(stack);
        features12.bufferDeviceAddress(true);
        features12.descriptorIndexing(true);

        IntBuffer pPhysicalDeviceCount = stack.mallocInt(1);
        VK14.vkEnumeratePhysicalDevices(instance, pPhysicalDeviceCount, null);

        PointerBuffer pPhysicalDevices = stack.mallocPointer(pPhysicalDeviceCount.get(0));
        VK14.vkEnumeratePhysicalDevices(instance, pPhysicalDeviceCount, pPhysicalDevices);

        int deviceCount = pPhysicalDeviceCount.get(0);

        VkPhysicalDevice bestDevice = null;
        int bestScore = 0;
        for (int i = 0; i < deviceCount; i++) {
            VkPhysicalDevice device = new VkPhysicalDevice(pPhysicalDevices.get(i), instance);
            if (!meetsRequirements(device, engine, createInfo)) continue;

            int score = scoreDevice(stack, device);
            if (score > bestScore) {
                bestScore = score;
                bestDevice = device;
            }
        }
        return bestDevice;
    }

    private boolean meetsRequirements(VkPhysicalDevice device, VKEngine engine, EngineCreateInfo createInfo) {
        VKCapabilitiesInstance c = device.getCapabilities();
        if (!createInfo.releaseMode && !c.VK_EXT_debug_utils) {
            return false;
        }
        return c.apiVersion >= 14;
        //todo: check if device supports all extensions and layers
    }

    private int scoreDevice(MemoryStack stack, VkPhysicalDevice device) {
        //todo: rank by hardware capabilities and driver
        return 0;
    }

    private ArrayList<ByteBuffer> enumerateExtensions(VKEngine engine, MemoryStack stack) {
        ArrayList<ByteBuffer> extensions = new ArrayList<>();

        String missingExtension = validateExtensions(stack, engineCreateInfo.vkExtensions, GLFWVulkan.glfwGetRequiredInstanceExtensions());
        if (missingExtension != null) {
            engine.throwException(new IllegalStateException("Missing extension %s".formatted(missingExtension)), HERE);
        }

        engineCreateInfo.vkExtensions.forEach(extension -> extensions.add(stack.UTF8(extension)));
        return extensions;
    }

    private String validateExtensions(MemoryStack stack, List<String> customExtensions, PointerBuffer glfwExtensions) {
        //assuming the capacity is the count
        int gltfCount = glfwExtensions.capacity();

        IntBuffer count = stack.mallocInt(1);
        VK14.vkEnumerateInstanceExtensionProperties((ByteBuffer) null, count, null);

        VkExtensionProperties.Buffer props = VkExtensionProperties.malloc(count.get(0), stack);

        VK14.vkEnumerateInstanceExtensionProperties((ByteBuffer) null, count, props);

        Iterable<String> iterable = () -> Utils.unwrapStrings(glfwExtensions, gltfCount);
        outer:
        for (String glfwExt : iterable) {
            for (VkExtensionProperties p : props) {
                String extName = p.extensionNameString();
                if (extName.equals(glfwExt)) continue outer;
            }
            return glfwExt;
        }
        outer:
        for (String ext : customExtensions) {
            for (VkExtensionProperties p : props) {
                String extName = p.extensionNameString();
                if (extName.equals(ext)) continue outer;
            }
            return ext;
        }

        return null;
    }

}
