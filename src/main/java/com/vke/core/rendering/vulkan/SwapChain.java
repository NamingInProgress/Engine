package com.vke.core.rendering.vulkan;

import com.vke.api.vulkan.SwapChainCreateInfo;
import com.vke.api.vulkan.VkPresentMode;
import com.vke.core.VKEngine;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.memory.intP;
import com.vke.utils.Disposable;
import com.vke.utils.Utils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

public class SwapChain implements Disposable {

    private static final String HERE = "SwapChain";

    private SwapChainCreateInfo info;
    private long swapChainHandle;
    private VkSurfaceFormatKHR.Buffer formats;
    private IntBuffer modes;
    private LongBuffer images;
    private VkSurfaceCapabilitiesKHR capabilities;
    private VkExtent2D extent;
    private VKEngine engine;

    private final AutoHeapAllocator alloc;

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
        int presentMode = choosePresentMode(pModes);
        VkExtent2D extent2D = chooseExtent(pCapabilities);
        int minImageCount = Math.max(3, pCapabilities.minImageCount());
        minImageCount = ( pCapabilities.maxImageCount() > 0 && minImageCount > pCapabilities.maxImageCount() ) ? pCapabilities.maxImageCount() : minImageCount;

        int imageCount = (pCapabilities.maxImageCount() > 0 && pCapabilities.minImageCount() + 1 > pCapabilities.maxImageCount()) ?
                pCapabilities.minImageCount() : pCapabilities.minImageCount() + 1;

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

        VulkanQueue graphicsQueue = device.getQueue(VulkanQueue.VkQueueType.GRAPHICS);
        VulkanQueue presentQueue = device.getQueue(VulkanQueue.VkQueueType.PRESENT);

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
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkExtent2D extent = VkExtent2D.malloc(stack);
            extent.width(width);
            extent.height(height);

            VkSwapchainCreateInfoKHR swapChainCreateInfo = VkSwapchainCreateInfoKHR.calloc(stack)
                    .imageExtent(extent)
                    .presentMode(pickPresentMode(vsync))
                    .oldSwapchain(this.swapChainHandle);

            createSwapChain(stack, info.logicalDevice, swapChainCreateInfo);
        }
    }

    private void createSwapChain(MemoryStack stack, LogicalDevice device, VkSwapchainCreateInfoKHR createInfo) {
        LongBuffer pSwapChain = stack.callocLong(1);

        if (KHRSwapchain.vkCreateSwapchainKHR(device.getDevice(), createInfo, null, pSwapChain) != VK14.VK_SUCCESS) {
            engine.throwException(new IllegalStateException("Failed to create Swap Chain!"), HERE);
        }

        this.swapChainHandle = pSwapChain.get(0);

        IntBuffer count = stack.ints(0);
        KHRSwapchain.vkGetSwapchainImagesKHR(device.getDevice(), swapChainHandle, count, null);

        LongBuffer images = alloc.allocLong(count.get(0)).getHeapObject();
        KHRSwapchain.vkGetSwapchainImagesKHR(device.getDevice(), swapChainHandle, count, images);
        this.images = images;
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
        if (!info.preferVsync) {
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

    @Override
    public void free() {
        alloc.close();
        KHRSwapchain.vkDestroySwapchainKHR(info.logicalDevice.getDevice(), swapChainHandle, null);
    }

    public long handle() {
        return this.swapChainHandle;
    }
}
