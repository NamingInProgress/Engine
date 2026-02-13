package com.vke.core.vulkan.device;

import com.vke.api.abstraction.RenderDevice;
import com.vke.api.abstraction.commands.CommandBuffer;
import com.vke.api.abstraction.data.Buffer;
import com.vke.api.abstraction.data.Sampler;
import com.vke.api.abstraction.data.Texture;
import com.vke.api.abstraction.descriptors.BackendType;
import com.vke.api.abstraction.descriptors.DeviceCapabilities;
import com.vke.api.abstraction.pipeline.ComputePipeline;
import com.vke.api.abstraction.pipeline.GraphicsPipeline;
import com.vke.api.abstraction.swapchain.Swapchain;
import com.vke.api.logger.LogLevel;
import com.vke.api.logger.Logger;
import com.vke.api.vulkan.pipeline.RenderPipeline;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.rendering.vulkan.VKUtils;
import com.vke.core.rendering.vulkan.createInfos.LogicalDeviceCreateInfo;
import com.vke.core.rendering.vulkan.createInfos.VulkanCreateInfo;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.rendering.vulkan.device.PhysicalDevice;
import com.vke.core.rendering.vulkan.device.VulkanQueue;
import com.vke.core.rendering.vulkan.frame.Frame;
import com.vke.core.vulkan.VulkanFrame;
import com.vke.core.vulkan.swapchain.VulkanSwapchain;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.ArrayList;

public class VulkanRenderDevice implements RenderDevice {

    private static final String HERE = "RenderDevice@VulkanImpl";

    private static final VkDebugUtilsMessengerCallbackEXTI debugMessengerCallback = (severity, type, pCallbackData, pUserData) -> {
        VkDebugUtilsMessengerCallbackDataEXT data = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);
        LoggerFactory.get("VK-Debug").log(LogLevel.fromVkMessageSeverity(severity), "%s: %s".formatted(VKUtils.getDebugMessageType(type), data.pMessageString()));
        return VK14.VK_FALSE;
    };

    private VkInstance instance;
    private PhysicalDevice physicalDevice;
    private LogicalDevice logicalDevice;
    private long debugMessenger, surface;
    private long vmaAllocator;

    private DeviceCapabilities cachedCapabilities;

    // Engine infos
    private final EngineCreateInfo engineCreateInfo;
    private final VulkanCreateInfo vulkanCreateInfo;
    private final VKEngine engine;

    private final AutoHeapAllocator alloc;

    private final Logger logger;

    public VulkanRenderDevice(VKEngine engine, EngineCreateInfo engineCreateInfo) {
        this.engine = engine;
        this.engineCreateInfo = engineCreateInfo;
        this.vulkanCreateInfo = engineCreateInfo.vulkanCreateInfo;
        this.alloc = new AutoHeapAllocator();
        this.logger = LoggerFactory.get("Vulkan Setup");

        initInstance();
        setupDebugMessenger(this.instance, engine);
        setupSurface();
        setupPhysicalDevice();
        initLogicalDevice();
        setupVmaAllocator();

        cachedCapabilities = physicalDevice.getDeviceCapabilities();
    }

    private void initInstance() {
        if (!engineCreateInfo.releaseMode) vulkanCreateInfo.extensions.add(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME);

        ArrayList<String> usedExtensions = new ArrayList<>();
        ArrayList<String> usedLayers = new ArrayList<>();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer validationLayers = null;

            PointerBuffer extensions = DeviceUtils.collectRequiredExtensions(alloc, vulkanCreateInfo.extensions, usedExtensions);

            if (engine.isDebugMode()) {
                validationLayers = DeviceUtils.collectValidationLayers(alloc, usedLayers);
            }

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

            this.instance = new VkInstance(pInstance.get(0), createInfo);
        }
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

    private void setupSurface() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pSurface = stack.mallocLong(1);
            GLFWVulkan.glfwCreateWindowSurface(instance, engine.getWindow().getHandle(), null, pSurface);
            surface = pSurface.get(0);
        }
    }

    private void setupPhysicalDevice() {
        physicalDevice = DeviceUtils.pickGpu(this.instance,
                logger,
                this.engineCreateInfo,
                this.vulkanCreateInfo.gpuExtensions);
        logger.info("Using GPU: " + physicalDevice.getName());
    }

    private void initLogicalDevice() {
        LogicalDeviceCreateInfo deviceCreateInfo = new LogicalDeviceCreateInfo();
        deviceCreateInfo.physicalDevice = physicalDevice.getDevice();
        deviceCreateInfo.physicalDeviceWrapper = physicalDevice;
        deviceCreateInfo.engineCreateInfo = engineCreateInfo;
        deviceCreateInfo.surfaceHandle = surface;
        logicalDevice = new LogicalDevice(engine, deviceCreateInfo);
    }

    private void setupVmaAllocator() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VmaVulkanFunctions vmaFuncs = VmaVulkanFunctions.calloc(stack)
                    .set(instance, logicalDevice.getDevice());

            VmaAllocatorCreateInfo vmaInfo = VmaAllocatorCreateInfo.calloc(stack)
                    .pVulkanFunctions(vmaFuncs)
                    .flags(Vma.VMA_ALLOCATOR_CREATE_BUFFER_DEVICE_ADDRESS_BIT)
                    .physicalDevice(physicalDevice.getDevice())
                    .device(logicalDevice.getDevice())
                    .instance(instance);

            PointerBuffer pVmaAlloc = stack.mallocPointer(1);
            Vma.vmaCreateAllocator(vmaInfo, pVmaAlloc);
            vmaAllocator = pVmaAlloc.get(0);
        }
    }

    @Override
    public BackendType backend() {
        return BackendType.VULKAN;
    }

    @Override
    public DeviceCapabilities capabilities() {
        return cachedCapabilities;
    }

    @Override
    public Buffer createBuffer(Buffer.Description info) {
        return null;
    }

    @Override
    public Texture createTexture(Texture.Description info) {
        return null;
    }

    @Override
    public Sampler createSampler(Sampler.Description info) {
        return null;
    }

    @Override
    public GraphicsPipeline createRenderPipeline(RenderPipeline.RenderPipelineBuilder builder) {
        return null;
    }

    @Override
    public ComputePipeline createComputePipeline() {
        throw new RuntimeException("Compute Pipelines are not yet implemented!");
    }

    @Override
    public CommandBuffer createCommandBuffer() {
        throw new RuntimeException("Non VulkanFrame Command Buffers are not implemented yet!");
    }

    @Override
    public void submit(CommandBuffer cmd, CommandBuffer.SubmitInfo info) {

    }

    @Override
    public void waitIdle() {
        VK14.vkDeviceWaitIdle(this.logicalDevice.getDevice());
    }

    @Override
    public VulkanSwapchain createSwapchain(Swapchain.Description info) {
        return new VulkanSwapchain(info, this, engine);
    }

    public VulkanFrame[] createFrames() {
        VulkanFrame[] frames = new VulkanFrame[vulkanCreateInfo.framesInFlight];

        for (int i = 0; i < vulkanCreateInfo.framesInFlight; i++) {
            frames[i] = new VulkanFrame(engine, logicalDevice);
        }

        return frames;
    }

    public VulkanFrame createImmediateFrame() {
        return new VulkanFrame(engine, logicalDevice);
    }

    @Override
    public void free() {
        logicalDevice.free();
        KHRSurface.vkDestroySurfaceKHR(instance, surface, null);
        Vma.vmaDestroyAllocator(vmaAllocator);
        if (debugMessenger != VK14.VK_NULL_HANDLE)
            EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, null);
        VK14.vkDestroyInstance(instance, null);
        alloc.close();
    }

    /** GETTERS **/
    public PhysicalDevice getPhysicalDevice() {
        return physicalDevice;
    }

    public LogicalDevice getLogicalDevice() {
        return logicalDevice;
    }

    public long getSurface() {
        return surface;
    }

    public VulkanQueue getQueue(VulkanQueue.Type type) {
        return logicalDevice.getQueue(type);
    }
}
