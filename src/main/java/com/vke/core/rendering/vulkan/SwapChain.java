package com.vke.core.rendering.vulkan;

import com.vke.api.vulkan.SwapChainCreateInfo;
import com.vke.api.vulkan.VkPresentMode;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.utils.Disposable;
import com.vke.utils.Utils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;

public class SwapChain implements Disposable {

    private SwapChainCreateInfo info;
    private KHRSwapchain swapchain;

    private AutoHeapAllocator alloc;

    public SwapChain(SwapChainCreateInfo createInfo) {
        this.info = createInfo;
        this.alloc = new AutoHeapAllocator();

        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkSurfaceCapabilitiesKHR pCap = VkSurfaceCapabilitiesKHR.calloc(stack);
            VkPhysicalDevice pd = createInfo.physicalDevice.getDevice();

            KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(pd, createInfo.surface, pCap);

            IntBuffer pFormatCount = stack.mallocInt(1);
            KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(pd, createInfo.surface, pFormatCount, null);
            int formatCount = pFormatCount.get(0);
            VkSurfaceFormatKHR.Buffer pFormat = VkSurfaceFormatKHR.calloc(formatCount, stack);
            KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(pd, createInfo.surface, pFormatCount, pFormat);

            IntBuffer pPresentCount = stack.mallocInt(1);
            KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(pd, createInfo.surface, pPresentCount, null);
            int presentCount = pPresentCount.get(0);
            IntBuffer pModes = stack.mallocInt(presentCount);
            KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(pd, createInfo.surface, pPresentCount, pModes);
        }
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

        }
    }
}
