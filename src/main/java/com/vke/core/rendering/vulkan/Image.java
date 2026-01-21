package com.vke.core.rendering.vulkan;

public class Image {
    private final long handle;

    public Image(long handle) {
        this.handle = handle;
    }

    public long getHandle() {
        return handle;
    }
}
