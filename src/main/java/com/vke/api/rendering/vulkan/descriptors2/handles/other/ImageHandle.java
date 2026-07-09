package com.vke.api.rendering.vulkan.descriptors2.handles.other;

import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.image.ImageBinding;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.core.vulkan.descriptor.ds2.DescriptorSetInstance;
import com.vke.core.vulkan.texture.VulkanTexture;

public class ImageHandle extends UniformHandle {

    public final ImageBinding imgBinding;
    public final int index;

    public ImageHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, ImageBinding imgBinding, int index) {
        super(group, set, binding, type, imgBinding);
        this.imgBinding = imgBinding;
        this.index = index;
    }

    public void set(Texture tex) {
        this.imgBinding.textures[index] = (VulkanTexture) tex;
        setDirty();
    }

}
