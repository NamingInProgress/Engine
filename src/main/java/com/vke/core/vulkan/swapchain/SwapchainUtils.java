package com.vke.core.vulkan.swapchain;

import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.vulkan.VKUtils;
import com.vke.core.vulkan.createInfos.VkPresentMode;
import com.vke.utils.Utils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;

public class SwapchainUtils {

    public static VkSurfaceFormatKHR chooseFormat(VkSurfaceFormatKHR.Buffer formats) {
        for (VkSurfaceFormatKHR format : formats) {
            if (format.format() == VK14.VK_FORMAT_B8G8R8A8_SRGB && format.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) return format;
        }

        return formats.get(0);
    }

    public static int choosePresentMode(IntBuffer pModes, boolean vsync) {
        int[] modes = Utils.acquireIntArrayFromBuffer(pModes);
        if (!vsync) {
            if (Utils.intsContain(modes, VkPresentMode.VK_PRESENT_MODE_MAILBOX_KHR)) {
                return VkPresentMode.VK_PRESENT_MODE_MAILBOX_KHR;
            }
        }
        return VkPresentMode.VK_PRESENT_MODE_FIFO_KHR;
    }

    public static VkExtent2D chooseExtent(VkSurfaceCapabilitiesKHR capabilities, AutoHeapAllocator alloc, long windowHandle) {
        if (capabilities.currentExtent().width() != -1) {
            return capabilities.currentExtent();
        }
        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            GLFW.glfwGetFramebufferSize(windowHandle, pWidth, pHeight);
            VkExtent2D e = VKUtils.clampExtent(alloc, pWidth.get(0), pHeight.get(0), capabilities.minImageExtent(), capabilities.maxImageExtent());
            System.out.println(e);
            return e;
        }
    }

    public static VkImageViewCreateInfo copyImageViewCreateInfo(VkImageViewCreateInfo original) {
        int size = VkImageViewCreateInfo.SIZEOF;
        long newAddr = MemoryUtil.nmemCalloc(1, size);
        MemoryUtil.memCopy(original.address(), newAddr, size);
        return VkImageViewCreateInfo.create(newAddr);
    }

}
