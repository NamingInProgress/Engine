package com.vke.core.vulkan.swapchain;

import com.vke.api.abstraction.data.Texture;
import com.vke.api.abstraction.data.TextureView;
import com.vke.api.abstraction.descriptors.texture.TextureFormat;

public class SwapchainImage implements Texture {

    private final long handle;
    private final TextureFormat format;

    public SwapchainImage(long handle, TextureFormat format) {
        this.handle = handle;
        this.format = format;
    }

    @Override
    public int width() {
        return 0;
    }

    @Override
    public int height() {
        return 0;
    }

    @Override
    public int depth() {
        return 0;
    }

    @Override
    public TextureFormat format() {
        return format;
    }

    @Override
    public int mipLevels() {
        return 0;
    }

    @Override
    public boolean isSwapchainImage() {
        return true;
    }

    @Override
    public TextureView createView(TextureView.Description info) { throw new RuntimeException("This method should not be called on SwapchainImage (createView)"); }

    public long getHandle() { return this.handle; }

    @Override
    public void free() {

    }
}
