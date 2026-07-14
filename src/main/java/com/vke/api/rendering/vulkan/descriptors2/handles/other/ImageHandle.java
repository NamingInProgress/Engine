package com.vke.api.rendering.vulkan.descriptors2.handles.other;

import com.vke.api.rendering.abstraction.renderer.data.ImageView;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.image.ImageBinding;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.core.rendering.vulkan.texture.VulkanImageView;

public class ImageHandle extends UniformHandle {

    public final ImageBinding imgBinding;
    public final int index;

    public ImageHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, ImageBinding imgBinding, int index) {
        super(group, set, binding, type, imgBinding);
        this.imgBinding = imgBinding;
        this.index = index;
    }

    public void set(Texture tex) {
        this.imgBinding.views[index] = (VulkanImageView) tex.defaultView();
        setDirty();
    }

    public void set(ImageView view) {
        this.imgBinding.views[index] = (VulkanImageView) view;
        setDirty();
    }

    public void nextWrite() {
        this.group.getSet(this.set).requestNewDescriptorSet();
    }

}
