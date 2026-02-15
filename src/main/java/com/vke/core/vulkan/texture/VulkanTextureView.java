package com.vke.core.vulkan.texture;

import com.vke.api.abstraction.data.Texture;
import com.vke.api.abstraction.data.TextureView;
import com.vke.api.abstraction.descriptors.texture.TextureFormat;
import com.vke.api.abstraction.descriptors.texture.TextureViewType;
import com.vke.api.vulkan.ImageLayout;

public class VulkanTextureView implements TextureView {

    @Override
    public Texture parent() {
        return null;
    }

    @Override
    public TextureFormat format() {
        return null;
    }

    @Override
    public TextureViewType type() {
        return null;
    }

    @Override
    public ImageLayout layout() {
        return null;
    }

    @Override
    public int baseMip() {
        return 0;
    }

    @Override
    public int mipCount() {
        return 0;
    }

    @Override
    public int baseLayer() {
        return 0;
    }

    @Override
    public int layerCount() {
        return 0;
    }

    @Override
    public void free() {

    }

}
