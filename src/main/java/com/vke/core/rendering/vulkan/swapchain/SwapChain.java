package com.vke.core.rendering.vulkan.swapchain;

import com.vke.api.vulkan.createInfos.SwapChainCreateInfo;
import com.vke.api.vulkan.createInfos.VkPresentMode;
import com.vke.core.VKEngine;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.memory.intP;
import com.vke.core.rendering.vulkan.VKUtils;
import com.vke.core.rendering.vulkan.VulkanQueue;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.rendering.vulkan.sync.Fence;
import com.vke.core.rendering.vulkan.sync.Semaphore;
import com.vke.utils.Disposable;
import com.vke.utils.Utils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;

public class SwapChain implements Disposable {

    private static final String HERE = "SwapChain";

    private SwapChainCreateInfo info;
    private long swapChainHandle;
    private VkSurfaceFormatKHR.Buffer formats;
    private IntBuffer modes;
    private VkSurfaceCapabilitiesKHR capabilities;
    private VkExtent2D extent;
    private VKEngine engine;
    private ArrayList<ImageView> imageViews = new ArrayList<>();
    private ArrayList<Image> images = new ArrayList<>();

    private int usedColorFormat;

    private final AutoHeapAllocator alloc;

    private int currentImageIndex = 0;

    public SwapChain(VKEngine engine, SwapChainCreateInfo createInfo) {
        this.engine = engine;
        this.info = createInfo;
        this.alloc = new AutoHeapAllocator();

        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkSurfaceCapabilitiesKHR pCap = alloc.allocStruct(VkSurfaceCapabilitiesKHR.SIZEOF, VkSurfaceCapabilitiesKHR::new);
            VkPhysicalDevice pd = createInfo.physicalDevice.getDevice();

            KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(pd, createInfo.surface, pCap);

            IntBuffer pFormatCount = stack.mallocInt(1);
            KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(pd, createInfo.surface, pFormatCount, null);
            int formatCount = pFormatCount.get(0);
            VkSurfaceFormatKHR.Buffer pFormat = alloc.allocBuffer(VkSurfaceFormatKHR.SIZEOF, formatCount, VkSurfaceFormatKHR.Buffer::new);
            KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(pd, createInfo.surface, pFormatCount, pFormat);

            IntBuffer pPresentCount = stack.mallocInt(1);
            KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(pd, createInfo.surface, pPresentCount, null);
            int presentCount = pPresentCount.get(0);
            intP pModesHeap = alloc.allocInt(presentCount);
            IntBuffer pModes = pModesHeap.getHeapObject();
            KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(pd, createInfo.surface, pPresentCount, pModes);

            this.capabilities = pCap;
            this.formats = pFormat;
            this.modes = pModes;

            VkSwapchainCreateInfoKHR swapChainCreateInfo = getSwapChainCreateInfo(stack, info.logicalDevice, formats, modes, capabilities);
            createSwapChain(stack, info.logicalDevice, swapChainCreateInfo);
        }
    }

    public VkSwapchainCreateInfoKHR getSwapChainCreateInfo(MemoryStack stack, LogicalDevice device, VkSurfaceFormatKHR.Buffer pFormat, IntBuffer pModes, VkSurfaceCapabilitiesKHR pCapabilities) {
        VkSurfaceFormatKHR format = chooseFormat(pFormat);
        usedColorFormat = format.format();
        int presentMode = choosePresentMode(pModes);
        VkExtent2D extent2D = chooseExtent(pCapabilities);
        this.extent = extent2D;
        int minImageCount = Math.max(3, pCapabilities.minImageCount());
        minImageCount = ( pCapabilities.maxImageCount() > 0 && minImageCount > pCapabilities.maxImageCount() ) ? pCapabilities.maxImageCount() : minImageCount;

        VkSwapchainCreateInfoKHR swapChainCreateInfo = VkSwapchainCreateInfoKHR.calloc(stack)
                .sType$Default()
                .surface(info.surface)
                .minImageCount(minImageCount)
                .imageFormat(format.format())
                .imageColorSpace(format.colorSpace())
                .imageExtent(extent2D)
                .imageArrayLayers(info.imageLayers)
                .imageUsage(VK14.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                .preTransform(pCapabilities.currentTransform())
                .compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                .presentMode(presentMode)
                .oldSwapchain(VK14.VK_NULL_HANDLE)
                .clipped(true);

        VulkanQueue graphicsQueue = device.getQueue(VulkanQueue.Type.GRAPHICS);
        VulkanQueue presentQueue = device.getQueue(VulkanQueue.Type.PRESENT);

        if (!graphicsQueue.equals(presentQueue)) {
            IntBuffer queueIndices = alloc.ints(graphicsQueue.index(), presentQueue.index()).getHeapObject();

            swapChainCreateInfo.imageSharingMode(VK14.VK_SHARING_MODE_CONCURRENT); // TODO: Replace with exclusive with memory transfers
            swapChainCreateInfo.queueFamilyIndexCount(2);
            swapChainCreateInfo.pQueueFamilyIndices(queueIndices);
        } else {
            IntBuffer queueIndex = alloc.ints(graphicsQueue.index()).getHeapObject();

            swapChainCreateInfo.imageSharingMode(VK14.VK_SHARING_MODE_EXCLUSIVE);
            swapChainCreateInfo.queueFamilyIndexCount(1);
            swapChainCreateInfo.pQueueFamilyIndices(queueIndex);
        }

        return swapChainCreateInfo;
    }

    public void recreate(boolean vsync, int width, int height) {
        this.imageViews.clear();
        this.images.clear();

        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkExtent2D extent = VkExtent2D.malloc(stack);
            extent.width(width);
            extent.height(height);

            VkSwapchainCreateInfoKHR swapChainCreateInfo = VkSwapchainCreateInfoKHR.calloc(stack)
                    .imageExtent(extent)
                    .presentMode(pickPresentMode(vsync))
                    .oldSwapchain(this.swapChainHandle);

            createSwapChain(stack, info.logicalDevice, swapChainCreateInfo);

            initImages(stack, info.logicalDevice);
        }
    }

    private void createSwapChain(MemoryStack stack, LogicalDevice device, VkSwapchainCreateInfoKHR createInfo) {
        LongBuffer pSwapChain = stack.callocLong(1);

        if (KHRSwapchain.vkCreateSwapchainKHR(device.getDevice(), createInfo, null, pSwapChain) != VK14.VK_SUCCESS) {
            engine.throwException(new IllegalStateException("Failed to create Swap Chain!"), HERE);
        }

        this.swapChainHandle = pSwapChain.get(0);

        this.initImages(stack, device);
    }

    private void initImages(MemoryStack stack, LogicalDevice device) {
        this.imageViews.clear();
        this.images.clear();

        IntBuffer count = stack.mallocInt(1);
        KHRSwapchain.vkGetSwapchainImagesKHR(device.getDevice(), swapChainHandle, count, null);

        LongBuffer images = alloc.allocLong(count.get(0)).getHeapObject();
        KHRSwapchain.vkGetSwapchainImagesKHR(device.getDevice(), swapChainHandle, count, images);

        for (int i = 0; i < count.get(0); i++) {
            this.images.add(new Image(images.get(i)));
        }

        VkImageSubresourceRange subresourceRange = VkImageSubresourceRange.calloc(stack)
                .aspectMask(VK14.VK_IMAGE_ASPECT_COLOR_BIT)
                .baseMipLevel(0)
                .levelCount(1)
                .baseArrayLayer(0)
                .layerCount(1);

        VkImageViewCreateInfo baseInfo = VkImageViewCreateInfo.calloc(stack)
                .viewType(VK14.VK_IMAGE_VIEW_TYPE_2D)
                .format(usedColorFormat)
                .subresourceRange(subresourceRange)
                .sType$Default();
        for (int i = 0; i < count.get(0); i++) {
            Image image = this.images.get(i);
            VkImageViewCreateInfo info = ImageView.copyCreateInfo(baseInfo);
            info.image(image.getHandle());
            ImageView view = new ImageView(image, engine, device, info);
            this.imageViews.add(view);
        }
    }

    public VkSurfaceFormatKHR chooseFormat(VkSurfaceFormatKHR.Buffer formats) {
        for (VkSurfaceFormatKHR format : formats) {
            if (format.format() == VK14.VK_FORMAT_B8G8R8A8_SRGB && format.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) return format;
        }

        return formats.get(0);
    }

    public int choosePresentMode(IntBuffer pModes) {
        int[] modes = Utils.acquireIntArrayFromBuffer(pModes);
        if (!info.preferVsync) {
            if (Utils.intsContain(modes, VkPresentMode.VK_PRESENT_MODE_MAILBOX_KHR)) {
                return VkPresentMode.VK_PRESENT_MODE_MAILBOX_KHR;
            }
        }
        return VkPresentMode.VK_PRESENT_MODE_FIFO_KHR;
    }

    private int pickPresentMode(boolean wantsVsync) {
        if (wantsVsync) {
            int[] modesArr = Utils.acquireIntArrayFromBuffer(modes);
            if (Utils.intsContain(modesArr, VkPresentMode.VK_PRESENT_MODE_MAILBOX_KHR)) {
                return VkPresentMode.VK_PRESENT_MODE_MAILBOX_KHR;
            }
        }
        return VkPresentMode.VK_PRESENT_MODE_FIFO_KHR;
    }

    public VkExtent2D chooseExtent(VkSurfaceCapabilitiesKHR capabilities) {
        if (capabilities.currentExtent().width() != Integer.MAX_VALUE) {
            return capabilities.currentExtent();
        }
        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            GLFW.glfwGetFramebufferSize(info.windowHandle, pWidth, pHeight);
            return VKUtils.clampExtent(alloc, pWidth.get(0), pHeight.get(0), capabilities.minImageExtent(), capabilities.maxImageExtent());
        }
    }

    public int nextImage(MemoryStack stack, Semaphore semaphore, Fence fence) {
        VkAcquireNextImageInfoKHR acquireInfo = VkAcquireNextImageInfoKHR.calloc(stack);
        acquireInfo
                .deviceMask(1)
                .swapchain(this.handle())
                .fence(fence != null ? fence.getHandle() : VK14.VK_NULL_HANDLE)
                .semaphore(semaphore.getHandle())
                .sType$Default();
        IntBuffer pNextImageIndex = stack.mallocInt(1);
        KHRSwapchain.vkAcquireNextImage2KHR(info.logicalDevice.getDevice(), acquireInfo, pNextImageIndex);
        currentImageIndex = pNextImageIndex.get(0);
        return currentImageIndex;
    }

    public int getColorFormat() {
        return usedColorFormat;
    }
    public ArrayList<Image> getImages() { return this.images; }
    public ArrayList<ImageView> getImageViews() { return this.imageViews; }
    public int currentImageIndex() { return this.currentImageIndex; }
    public VkExtent2D getExtent() {
        return extent;
    }
    public int getImageCount() { return this.images.size(); }

    @Override
    public void free() {
        alloc.close();
        KHRSwapchain.vkDestroySwapchainKHR(info.logicalDevice.getDevice(), swapChainHandle, null);
        this.imageViews.forEach(ImageView::free);
    }

    public long handle() {
        return this.swapChainHandle;
    }
}
