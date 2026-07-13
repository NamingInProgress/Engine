package com.vke.api.rendering.vulkan.descriptors.bindings.image;

import com.vke.api.rendering.vulkan.descriptors.bindings.DescriptorBinding;
import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.core.rendering.vulkan.texture.texture2.VulkanImageView;

public class ImageBinding extends DescriptorBinding {

    public final VulkanImageView[] views;

    public ImageBinding(BindingLayout layout) {
        super(layout);

        this.views = new VulkanImageView[layout.descriptorCount];
    }

}
