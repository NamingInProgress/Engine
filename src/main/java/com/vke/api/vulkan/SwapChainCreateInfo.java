package com.vke.api.vulkan;

import com.vke.core.rendering.vulkan.PhysicalDevice;

public class SwapChainCreateInfo {
    public PhysicalDevice physicalDevice;
    public long surface;
    public boolean preferVsync;
    public long windowHandle;
}
