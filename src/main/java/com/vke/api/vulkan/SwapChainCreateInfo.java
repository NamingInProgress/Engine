package com.vke.api.vulkan;

import com.vke.core.rendering.vulkan.LogicalDevice;
import com.vke.core.rendering.vulkan.PhysicalDevice;

public class SwapChainCreateInfo {
    public PhysicalDevice physicalDevice;
    public LogicalDevice logicalDevice;
    public long surface;
    public boolean preferVsync;
    public long windowHandle;
    public int imageLayers = 1;
    public int imageViewType = 2;
}
