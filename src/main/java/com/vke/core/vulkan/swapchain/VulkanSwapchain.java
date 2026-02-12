package com.vke.core.vulkan.swapchain;

import com.vke.api.abstraction.IntEnum;
import com.vke.api.abstraction.data.Texture;
import com.vke.api.abstraction.descriptors.BackendType;
import com.vke.api.abstraction.descriptors.texture.TextureFormat;
import com.vke.api.abstraction.swapchain.Swapchain;
import com.vke.api.abstraction.sync.Semaphore;
import com.vke.api.logger.Logger;
import com.vke.core.VKEngine;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.memory.intP;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.rendering.vulkan.device.VulkanQueue;
import com.vke.core.vulkan.VulkanRenderDevice;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

public class VulkanSwapchain implements Swapchain {

    private static final String HERE = "Swapchain@VulkanImpl";

    private long swapchain;

    private VkSurfaceCapabilitiesKHR capabilities;
    private VkSurfaceFormatKHR.Buffer formats;
    private IntBuffer modes;

    private VkExtent2D extent;

    private TextureFormat format;
    private boolean vsync;
    private long surface;

    // Engine Infos
    private final VKEngine engine;
    private final VulkanRenderDevice device;
    private final long windowHandle;

    private final AutoHeapAllocator alloc;

    private final Logger logger;

    public VulkanSwapchain(Description description) {
        if (!(description.device() instanceof VulkanRenderDevice)) description.engine()
                .throwException(new IllegalStateException("Provided incorrect device for backend type: %s (Provided: %s)".formatted(BackendType.VULKAN, description.device().backend())), HERE);

        this.device = (VulkanRenderDevice) description.device();
        this.engine = description.engine();
        this.vsync = description.vsync();
        this.windowHandle = description.windowHandle();
        this.surface = device.getSurface();
        this.alloc = new AutoHeapAllocator();
        this.logger = LoggerFactory.get("Vulkan Setup");

        setupInfoStructs(device.getPhysicalDevice().getDevice(), device.getSurface());
    }

    private void setupInfoStructs(VkPhysicalDevice device, long surface) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkSurfaceCapabilitiesKHR pCap = alloc.allocStruct(VkSurfaceCapabilitiesKHR.SIZEOF, VkSurfaceCapabilitiesKHR::new);

            KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, pCap);

            IntBuffer pFormatCount = stack.mallocInt(1);
            KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, pFormatCount, null);
            int formatCount = pFormatCount.get(0);
            VkSurfaceFormatKHR.Buffer pFormat = alloc.allocBuffer(VkSurfaceFormatKHR.SIZEOF, formatCount, VkSurfaceFormatKHR.Buffer::new);
            KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, pFormatCount, pFormat);

            IntBuffer pPresentCount = stack.mallocInt(1);
            KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, pPresentCount, null);
            int presentCount = pPresentCount.get(0);
            intP pModesHeap = alloc.allocInt(presentCount);
            IntBuffer pModes = pModesHeap.getHeapObject();
            KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, pPresentCount, pModes);

            this.capabilities = pCap;
            this.formats = pFormat;
            this.modes = pModes;
        }
    }

    private VkSwapchainCreateInfoKHR getCreateInfo(MemoryStack stack) {
        VkSwapchainCreateInfoKHR info = VkSwapchainCreateInfoKHR.calloc(stack);

        VkSurfaceFormatKHR pickedFormat = SwapchainUtils.chooseFormat(formats);
        int presentMode = SwapchainUtils.choosePresentMode(modes, vsync);
        VkExtent2D extent2D = SwapchainUtils.chooseExtent(capabilities, alloc, windowHandle);
        int minImageCount = Math.max(3, capabilities.minImageCount());
        minImageCount = ( capabilities.maxImageCount() > 0 && minImageCount > capabilities.maxImageCount() ) ? capabilities.maxImageCount() : minImageCount;

        info.sType$Default()
                .surface(surface)
                .minImageCount(minImageCount)
                .imageFormat(pickedFormat.format())
                .imageColorSpace(pickedFormat.colorSpace())
                .imageExtent(extent2D)
                .imageArrayLayers(1)
                .imageUsage(VK14.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                .preTransform(capabilities.currentTransform())
                .compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                .presentMode(presentMode)
                .oldSwapchain(VK14.VK_NULL_HANDLE)
                .clipped(true);

        VulkanQueue graphicsQueue = device.getQueue(VulkanQueue.Type.GRAPHICS);
        VulkanQueue presentQueue = device.getQueue(VulkanQueue.Type.PRESENT);

        if (!graphicsQueue.equals(presentQueue)) {
            IntBuffer queueIndices = stack.ints(graphicsQueue.index(), presentQueue.index()); // replaced alloc!

            info.imageSharingMode(VK14.VK_SHARING_MODE_CONCURRENT); // TODO: Replace with exclusive with memory transfers
            info.queueFamilyIndexCount(2);
            info.pQueueFamilyIndices(queueIndices);
        } else {
            IntBuffer queueIndex = alloc.ints(graphicsQueue.index()).getHeapObject();

            info.imageSharingMode(VK14.VK_SHARING_MODE_EXCLUSIVE);
            info.queueFamilyIndexCount(1);
            info.pQueueFamilyIndices(queueIndex);
        }

        this.format = IntEnum.fromInt(TextureFormat.values(), pickedFormat.format());

        return info;
    }

    private void createSwapchain(LogicalDevice device, VkSwapchainCreateInfoKHR createInfo) {
        LongBuffer pSwapChain = stack.callocLong(1);

        if (KHRSwapchain.vkCreateSwapchainKHR(device.getDevice(), createInfo, null, pSwapChain) != VK14.VK_SUCCESS) {
            engine.throwException(new IllegalStateException("Failed to create Swap Chain!"), HERE);
        }

        this.swapchain = pSwapChain.get(0);
    }

    @Override
    public int width() {
        return extent.width();
    }

    @Override
    public int height() {
        return extent.height();
    }

    @Override
    public TextureFormat format() {
        return this.format;
    }

    @Override
    public int acquireNextImage(Semaphore imageAvailable) {
        return 0;
    }

    @Override
    public Texture getImage(int index) {
        return null;
    }

    @Override
    public void present(Semaphore renderFinished) {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void recreate() {

    }

    @Override
    public void destroy() {
        KHRSwapchain.vkDestroySwapchainKHR(device.getLogicalDevice().getDevice(), this.swapchain, null);
        // clear image views
    }

    @Override
    public void free() {
        this.destroy();
        alloc.close();
    }

    public boolean vsync() { return this.vsync; }

    public void setVsync(boolean vsync) {
        this.device.waitIdle();
        this.vsync = vsync;
        recreate();
    }


}
