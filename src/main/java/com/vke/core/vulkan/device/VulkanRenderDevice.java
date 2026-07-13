package com.vke.core.vulkan.device;

import com.vke.api.rendering.vulkan.pipeline.ComputePipelineData;
import com.vke.api.rendering.vulkan.pipeline.RenderPipelineData;
import com.vke.api.rendering.abstraction.RenderDevice;
import com.vke.api.rendering.abstraction.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.data.GpuBuffer;
import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.enums.BackendType;
import com.vke.api.rendering.abstraction.enums.DeviceCapabilities;
import com.vke.api.rendering.abstraction.enums.QueueType;
import com.vke.api.rendering.abstraction.enums.ShaderType;
import com.vke.api.rendering.abstraction.swapchain.Swapchain;
import com.vke.api.logger.LogLevel;
import com.vke.api.logger.Logger;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.assets.pipeline.protocols.shader.ShaderPreprocessor;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.services2.Services;
import com.vke.core.vulkan.service.VulkanRenderSystem;
import com.vke.core.vulkan.service.VulkanRenderer;
import com.vke.core.vulkan.shader.service.ShaderCompiler;
import com.vke.core.vulkan.shr.service.ShaderReflector;
import com.vke.core.vulkan.texture.texture2.VulkanTexture;
import com.vke.core.vulkan.utils.VKUtils;
import com.vke.core.vulkan.buffers.VulkanGpuBuffer;
import com.vke.core.vulkan.createInfos.LogicalDeviceCreateInfo;
import com.vke.core.vulkan.createInfos.VulkanCreateInfo;
import com.vke.core.vulkan.VulkanFrame;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.core.vulkan.pipeline.VulkanComputePipeline;
import com.vke.core.vulkan.pipeline.VulkanRenderPipeline;
import com.vke.core.vulkan.sampler.VulkanSampler;
import com.vke.core.vulkan.shader.VulkanShader;
import com.vke.core.vulkan.swapchain.VulkanSwapchain;
import com.vke.core.vulkan.sync.VulkanFence;
import com.vke.core.vulkan.sync.VulkanSemaphore;
import com.vke.utils.io.Disposable;
import com.vke.utils.tuple.Pair;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.*;
import com.vke.utils.io.Identifier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

public class VulkanRenderDevice implements RenderDevice {

    private static final String HERE = "RenderDevice@VulkanImpl";

    private static final VkDebugUtilsMessengerCallbackEXTI debugMessengerCallback = (severity, type, pCallbackData, pUserData) -> {
        VkDebugUtilsMessengerCallbackDataEXT data = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);
        LoggerFactory.get("VK-Debug").log(LogLevel.fromVkMessageSeverity(severity), "%s: %s".formatted(VKUtils.getDebugMessageType(type), data.pMessageString()));
        return VK14.VK_FALSE;
    };

    private static final AtomicLong SHADER_ID = new AtomicLong();

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
    private final VulkanRenderSystem context;
    private final VulkanRenderer renderer;

    private final AutoHeapAllocator alloc;

    private final Logger logger;

    private final Queue<Disposable> FREE_QUEUE = new ArrayDeque<>();

    public VulkanRenderDevice(VulkanRenderSystem context) {
        this.engine = context.getEngine();
        this.context = context;
        this.renderer = context.renderer();
        this.engineCreateInfo = renderer.getCreateInfo();
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

    public VulkanGpuBuffer createBuffer(GpuBuffer.Description info) {
        return new VulkanGpuBuffer(context, info);
    }

    @Override
    public VulkanTexture createTexture(Texture.TextureDesc info) {
        return new VulkanTexture(this.context, info);
    }

    @Override
    public VulkanSampler createSampler(Sampler.Description info) {
        return new VulkanSampler(this.context, info);
    }

    @Override
    public VulkanShader createShader(Identifier identifier, ShaderType shaderType) throws IOException {
        Pair<String, ShaderPreprocessor.ShaderMetadata> processed = ShaderPreprocessor.getInstance(getRenderer()).process(identifier);
        byte[] bytes = processed.v1.getBytes(StandardCharsets.UTF_8);

        try {
            ByteBuffer spirv = engine.<ShaderCompiler>service(Services.SHADER_COMPILER)
                    .compileGlslToSpirV(bytes, shaderType, identifier);

            VulkanShader shader = new VulkanShader(context, spirv, shaderType, SHADER_ID.get());
            logger.trace("Creating Shader " + identifier + " for ID: " + SHADER_ID.get());

            // Only caches the IR and caches the reflected shader so the performance cost is negligible.
            engine.<ShaderReflector>service(Services.SHADER_REFLECTION).reflect(SHADER_ID.getAndIncrement(), identifier, spirv, shaderType, processed.v2);

            return shader;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public VulkanCmdBuffers createCommandBuffer() {
        throw new RuntimeException("Non VulkanFrame Command Buffers are not implemented yet!");
    }

    @Override
    public void submit(CommandBuffer buffers, CommandBuffer.SubmitInfo info) {
        if (!(buffers instanceof VulkanCmdBuffers)) throw new IllegalStateException("Provided non vulkan command buffers object to vulkan render device!");
        VulkanCmdBuffers cmd = (VulkanCmdBuffers) buffers;
        long pFence = info.getFence() == null ? VK14.VK_NULL_HANDLE : ((VulkanFence) info.getFence()).getHandle();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferSubmitInfo.Buffer cmdSubmitInfo = VkCommandBufferSubmitInfo.calloc(1, stack);
            cmdSubmitInfo.get(0)
                    .sType$Default()
                    .deviceMask(0)
                    .commandBuffer(cmd.getBuffer());

            VkSemaphoreSubmitInfo.Buffer waitInfo = null;
            VkSemaphoreSubmitInfo.Buffer signalInfo = null;

            if (!info.isImmediate()) {
                waitInfo = VkSemaphoreSubmitInfo.calloc(1, stack);
                signalInfo = VkSemaphoreSubmitInfo.calloc(1, stack);

                waitInfo.put(0, VulkanSemaphore.getDefaultSubmitInfo(stack, (VulkanSemaphore) info.getImageSemaphore(), (int) VK14.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT));
                signalInfo.put(0, VulkanSemaphore.getDefaultSubmitInfo(stack, (VulkanSemaphore) info.getPresentSemaphore(), (int) VK14.VK_PIPELINE_STAGE_2_ALL_GRAPHICS_BIT));
            }

            VkSubmitInfo2.Buffer submitInfo = VkSubmitInfo2.calloc(1, stack);
            submitInfo.get(0)
                    .sType$Default()
                    .pCommandBufferInfos(cmdSubmitInfo)
                    .pWaitSemaphoreInfos(waitInfo)
                    .pSignalSemaphoreInfos(signalInfo);

            VkQueue queue = this.logicalDevice.getQueue(info.getType()).vk();
            if (VK14.vkQueueSubmit2(queue, submitInfo, pFence) != VK14.VK_SUCCESS) {
                logger.warn("Failed to submit queue!");
            }
        }
    }

    @Override
    public VulkanRenderPipeline createRenderPipeline(RenderPipelineData data) {
        return new VulkanRenderPipeline(context, data);
    }

    @Override
    public VulkanComputePipeline createComputePipeline(ComputePipelineData data) {
        return new VulkanComputePipeline(context, data);
    }

    @Override
    public void waitIdle() {
        VK14.vkDeviceWaitIdle(this.logicalDevice.getDevice());
    }

    @Override
    public VulkanSwapchain createSwapchain(Swapchain.Description info) {
        return new VulkanSwapchain(info, context);
    }

    public VulkanFrame[] createFrames() {
        VulkanFrame[] frames = new VulkanFrame[vulkanCreateInfo.framesInFlight];

        for (int i = 0; i < vulkanCreateInfo.framesInFlight; i++) {
            frames[i] = new VulkanFrame(context, getRenderer().getFrameCounter());
        }

        return frames;
    }

    public VulkanFrame createImmediateFrame() {
        return new VulkanFrame(context, getRenderer().getFrameCounter(), true);
    }

    @Override
    public void free() {
        KHRSurface.vkDestroySurfaceKHR(instance, surface, null);
        Vma.vmaDestroyAllocator(vmaAllocator);
        if (debugMessenger != VK14.VK_NULL_HANDLE)
            EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, null);
        logicalDevice.free();
        VK14.vkDestroyInstance(instance, null);
        alloc.close();
        FREE_QUEUE.forEach(Disposable::free);
    }

    /** GETTERS **/
    public PhysicalDevice getPhysicalDevice() {
        return physicalDevice;
    }

    public LogicalDevice getLogicalDevice() {
        return logicalDevice;
    }

    public VkDevice vkLogicalDevice() { return logicalDevice.getDevice(); }
    public VkPhysicalDevice physicalDevice() { return physicalDevice.getDevice(); }

    public long getSurface() {
        return surface;
    }

    public VulkanQueue getQueue(QueueType type) {
        return logicalDevice.getQueue(type);
    }

    public long getVmaAllocator() {
        return vmaAllocator;
    }

    public VKEngine getEngine() { return this.engine; }

    public VulkanRenderer getRenderer() {
        return renderer;
    }
}
