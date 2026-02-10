package com.vke.core.rendering.vulkan.swapchain;

public class SwapchainImage {
    private final long handle;

    public SwapchainImage(long handle) {
        this.handle = handle;
    }

    public long getHandle() {
        return handle;
    }
}
