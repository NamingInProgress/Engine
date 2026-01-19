package com.vke.core.rendering.vulkan;

import com.vke.api.vulkan.SwapChainCreateInfo;
import com.vke.api.vulkan.VkPresentMode;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.memory.intP;
import com.vke.utils.Disposable;
import com.vke.utils.Utils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;

public class SwapChain implements Disposable {

    private SwapChainCreateInfo info;
    private KHRSwapchain swapChain;
    private VkSurfaceFormatKHR.Buffer formats;
    private IntBuffer modes;
    private VkSurfaceCapabilitiesKHR capabilities;
    private VkExtent2D extent;

    private final AutoHeapAllocator alloc;

    public SwapChain(SwapChainCreateInfo createInfo) {
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
        }
    }

    public VkSwapchainCreateInfoKHR getSwapChainCreateInfo(MemoryStack stack, VkSurfaceFormatKHR.Buffer pFormat, IntBuffer pModes, VkSurfaceCapabilitiesKHR pCapabilities) {
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
                .imageArrayLayers(info.layers)
                .imageUsage(VK14.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                .preTransform(pCapabilities.currentTransform())
                .compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                .presentMode(presentMode)
                .clipped(true);

        if ()

        return swapChainCreateInfo;
    }

    public SwapChain recreate(boolean vsync, int width, int height) {

    }

    public VkSurfaceFormatKHR chooseFormat(VkSurfaceFormatKHR.Buffer formats) {
        for (VkSurfaceFormatKHR format : formats) {
            if (format.format() == VK14.VK_FORMAT_B8G8R8A8_SRGB && format.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) return format;
        }

        return formats.get(0);
    }

    public int choosePresentMode(IntBuffer pModes) {
        int[] modes = pModes.array();
        if (!info.preferVsync) {
            if (Utils.intsContain(modes, VkPresentMode.VK_PRESENT_MODE_MAILBOX_KHR)) {
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
    }

}
