package com.vke.api.vulkan;

import com.vke.core.rendering.vulkan.PhysicalDevice;
import com.vke.core.rendering.vulkan.VulkanQueue;

import java.util.function.ToIntFunction;

public class SwapChainCreateInfo {
    public PhysicalDevice physicalDevice;
    public long surface;
    public boolean preferVsync;
    public long windowHandle;
    public int layers = 1;
    public ToIntFunction<VulkanQueue.VkQueueType> queueFamilyIndexOf;
}
