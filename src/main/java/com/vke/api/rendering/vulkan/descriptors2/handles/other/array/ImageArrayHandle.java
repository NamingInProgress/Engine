package com.vke.api.rendering.vulkan.descriptors2.handles.other.array;

import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.image.ImageBinding;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.core.vulkan.texture.VulkanTexture;

public class ImageArrayHandle extends UniformHandle {

    public final ImageBinding imgBinding;

    public ImageArrayHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, ImageBinding imgBinding) {
        super(group, set, binding, type, imgBinding);
        this.imgBinding = imgBinding;
    }

    public void set(Texture tex, int index) {
        this.imgBinding.textures[index] = (VulkanTexture) tex;
        setDirty();
    }

}
