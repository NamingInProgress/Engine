package com.vke.core.vulkan.swapchain;

public class SwapchainImage {

    private final long handle;

    public SwapchainImage(long handle) {
        this.handle = handle;
    }

    public long getHandle() { return this.handle; }

}
