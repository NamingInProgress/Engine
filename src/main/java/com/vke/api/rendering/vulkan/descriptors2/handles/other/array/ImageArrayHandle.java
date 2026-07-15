package com.vke.api.rendering.vulkan.descriptors2.handles.other.array;

import com.vke.api.rendering.abstraction.renderer.data.ImageView;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.other.array.ImageArrayResource;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.image.ImageBinding;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.core.rendering.vulkan.texture.VulkanImageView;

public class ImageArrayHandle extends UniformHandle implements ImageArrayResource {

    public final ImageBinding imgBinding;

    public ImageArrayHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, ImageBinding imgBinding) {
        super(group, set, binding, type, imgBinding);
        this.imgBinding = imgBinding;
    }

    @Override
    public void set(int index, ImageView view) {
        this.imgBinding.views[index] = (VulkanImageView) view;
        setDirty();
    }

    @Override
    public void nextWrite() {
        this.group.getSet(this.set).requestNewDescriptorSet();
    }

}
