package com.vke.core.rendering.vulkan;

import com.vke.api.logger.LogLevel;
import com.vke.api.logger.Logger;
import com.vke.api.vulkan.createInfos.LogicalDeviceCreateInfo;
import com.vke.api.vulkan.createInfos.SwapChainCreateInfo;
import com.vke.api.vulkan.createInfos.VulkanCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.memory.charPP;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.rendering.vulkan.device.PhysicalDevice;
import com.vke.core.rendering.vulkan.swapchain.SwapChain;
import com.vke.utils.Disposable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VulkanSetup implements Disposable {

    private static final String HERE = "Vulkan Init";

    private static final VkDebugUtilsMessengerCallbackEXTI debugMessengerCallback = (severity, type, pCallbackData, pUserData) -> {
        VkDebugUtilsMessengerCallbackDataEXT data = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);
        LoggerFactory.get("VK-Debug").log(LogLevel.fromVkMessageSeverity(severity), "%s: %s".formatted(VKUtils.getDebugMessageType(type), data.pMessageString()));
        return VK14.VK_FALSE;
    };

    private final EngineCreateInfo engineCreateInfo;
    private final VulkanCreateInfo vulkanCreateInfo;

    private AutoHeapAllocator alloc;


    private VkInstance instance;
    private PhysicalDevice physicalDevice;
    private LogicalDevice logicalDevice;
    private SwapChain swapChain;
    private long surface, debugMessenger;
    private Frame[] frames;


    public VulkanSetup(EngineCreateInfo engineCreateInfo) {
        this.engineCreateInfo = engineCreateInfo;
        this.vulkanCreateInfo = engineCreateInfo.vulkanCreateInfo;
    }

    @SuppressWarnings("all")
    public void initVulkan(VKEngine engine) {
        if (!engineCreateInfo.releaseMode) vulkanCreateInfo.extensions.add(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME);

        ArrayList<String> usedExtensions = new ArrayList<>();
        ArrayList<String> usedLayers = new ArrayList<>();

        alloc = new AutoHeapAllocator();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer validationLayers = collectValidationLayers(alloc, engine, usedLayers);
            PointerBuffer extensions = collectRequiredExtensions(alloc, engine, usedExtensions);

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
                    .ppEnabledExtensionNames(extensions);

            if (validationLayers != null) {
                createInfo.ppEnabledLayerNames(validationLayers);
            }

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
            physicalDevice = pickGpu(engine, engineCreateInfo, vulkanCreateInfo.gpuExtensions);
            engine.getLogger().info("Using GPU: " + physicalDevice.getName());

            LogicalDeviceCreateInfo deviceCreateInfo = new LogicalDeviceCreateInfo();
            deviceCreateInfo.physicalDevice = physicalDevice.getDevice();
            deviceCreateInfo.physicalDeviceWrapper = physicalDevice;
            deviceCreateInfo.engineCreateInfo = engineCreateInfo;
            deviceCreateInfo.surfaceHandle = surface;
            logicalDevice = new LogicalDevice(engine, deviceCreateInfo);

            /**  Swapchain Setup  **/
            SwapChainCreateInfo swapChainCreateInfo = new SwapChainCreateInfo();
            swapChainCreateInfo.preferVsync = engineCreateInfo.vsync;
            swapChainCreateInfo.logicalDevice = logicalDevice;
            swapChainCreateInfo.physicalDevice = physicalDevice;
            swapChainCreateInfo.surface = surface;
            swapChainCreateInfo.windowHandle = engine.getWindow().getHandle();
            //check if create info is filled completely
            //todo: update these nunbers in the future:
            //  1st num: amount of fields in create info
            //  2nd num: amount of fields set
            if (5 == 5) {
                swapChain = new SwapChain(engine, swapChainCreateInfo);
            }

            frames = new Frame[vulkanCreateInfo.framesInFlight];

            for (int i = 0; i < vulkanCreateInfo.framesInFlight; i++) {
                frames[i] = new Frame(engine, logicalDevice);
            }
        } catch (Exception e) {
            engine.throwException(e, HERE);
        }
    }

    @Override
    public void free() {
        Arrays.stream(frames).forEach(Frame::free);
        if (debugMessenger != VK14.VK_NULL_HANDLE) {
            EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, null);
        }
        KHRSurface.vkDestroySurfaceKHR(instance, surface, null);
        EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, null);
        VK14.vkDestroyInstance(instance, null);
        if (alloc != null) alloc.close();
    }

    private void setupDebugMessenger(VkInstance instance, VKEngine engine) {
        if (engineCreateInfo.releaseMode) return;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDebugUtilsMessengerCreateInfoEXT debugMessengerCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack)
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

    private PhysicalDevice pickGpu(VKEngine engine, EngineCreateInfo createInfo, List<String> extensions) {
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

                if (!meetsRequirements(d, engine, createInfo, extensions)) continue;

                int score = scoreDevice(d);
                if (score > bestScore) {
                    bestScore = score;
                    bestDevice = d;
                }
            }

            return bestDevice;
        }
    }

    private boolean meetsRequirements(PhysicalDevice device, VKEngine engine, EngineCreateInfo createInfo, List<String> extensions) {
        VKCapabilitiesInstance c = device.getCapabilities();

        if (!createInfo.releaseMode && !c.VK_EXT_debug_utils) {
            return false;
        }
        if (c.apiVersion < 14) {
            return false;
        }

        if (!validateRequiredQueueFamilies(device, vulkanCreateInfo.requiredQueueFamilyBits)) {
            return false;
        }

        String missingExt = validateRequestedExtensions(device.getExtensionsBuffer(), extensions);
        if (missingExt != null) {
            Logger logger = engine.getLogger();
            logger.info("Couldn't select %s, because it doesn't support extension %s!", device.getName(), missingExt);
            return false;
        }

        return true;
    }

    private int scoreDevice(PhysicalDevice device) {
        int score = 0;
        VkPhysicalDeviceProperties properties = device.getProperties();

        if (properties.deviceType() == VK14.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) {
            score += 1000;
        }

        VkPhysicalDeviceLimits limits = properties.limits();
        score += limits.maxImageDimension2D();
        score += limits.maxColorAttachments() * 10;
        score += limits.maxBoundDescriptorSets() * 10;

        return score;
    }

    private PointerBuffer collectRequiredExtensions(AutoHeapAllocator alloc, VKEngine engine, List<String> usedExtensionsOut) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ArrayList<String> stringExtensions = new ArrayList<>(vulkanCreateInfo.extensions);
            VKUtils.getGlfwExtensionNames(stack).forEachRemaining(stringExtensions::add);

            IntBuffer count = stack.mallocInt(1);
            VK14.vkEnumerateInstanceExtensionProperties((ByteBuffer) null, count, null);

            VkExtensionProperties.Buffer props = VkExtensionProperties.malloc(count.get(0), stack);
            VK14.vkEnumerateInstanceExtensionProperties((ByteBuffer) null, count, props);

            String missingExtension = validateRequestedExtensions(props, stringExtensions);
            if (missingExtension != null) {
                engine.throwException(new IllegalStateException("Missing extension %s".formatted(missingExtension)), HERE);
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

    private PointerBuffer collectValidationLayers(AutoHeapAllocator alloc, VKEngine engine, List<String> usedLayerOut) {
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

    private boolean validateRequiredQueueFamilies(PhysicalDevice device, int requireQueueFlagBits) {
        VkQueueFamilyProperties.Buffer queueFamilies = device.getQueueFamilyBuffer();

        int a = 0;

        for (VkQueueFamilyProperties props : queueFamilies) {
            a |= props.queueFlags();
        }

        return (a & requireQueueFlagBits) == requireQueueFlagBits;
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

    public VkInstance getInstance() {
        return instance;
    }

    public LogicalDevice getLogicalDevice() {
        return logicalDevice;
    }

    public SwapChain getSwapChain() {
        return swapChain;
    }

    public long getSurface() {
        return surface;
    }

    public long getDebugMessenger() {
        return debugMessenger;
    }

    public Frame[] getFrames() { return this.frames; }

}
