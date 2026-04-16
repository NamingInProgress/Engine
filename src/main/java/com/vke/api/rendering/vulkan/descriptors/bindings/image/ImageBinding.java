package com.vke.api.rendering.vulkan.descriptors.bindings.image;

import com.vke.api.rendering.vulkan.descriptors.bindings.DescriptorBinding;
import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.core.vulkan.texture.VulkanTexture;

public class ImageBinding extends DescriptorBinding {

    public final VulkanTexture[] textures;

    public ImageBinding(BindingLayout layout) {
        super(layout);

        this.textures = new VulkanTexture[layout.descriptorCount];
    }

    @Override
    public <T extends DescriptorBinding> T copy() {
        return (T) new ImageBinding(layout);
    }

}
