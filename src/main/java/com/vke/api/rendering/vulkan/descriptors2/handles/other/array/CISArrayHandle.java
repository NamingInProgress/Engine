package com.vke.api.rendering.vulkan.descriptors2.handles.other.array;

import com.vke.api.rendering.abstraction.renderer.data.ImageView;
import com.vke.api.rendering.abstraction.renderer.data.Sampler;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.other.array.CISArrayResource;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.CombinedImageSamplerBinding;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.core.rendering.vulkan.sampler.VulkanSampler;
import com.vke.core.rendering.vulkan.texture.VulkanImageView;

public class CISArrayHandle extends UniformHandle implements CISArrayResource {

    public final CombinedImageSamplerBinding cisBinding;

    public CISArrayHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, CombinedImageSamplerBinding cisBinding) {
        super(group, set, binding, type, cisBinding);
        this.cisBinding = cisBinding;
    }

    @Override
    public void set(int index, ImageView view, Sampler sampler) {
        this.cisBinding.views[index] = (VulkanImageView) view;
        this.cisBinding.samplers[index] = (VulkanSampler) sampler;
        setDirty();
    }

    @Override
    public void nextWrite() {
        this.group.getSet(this.set).requestNewDescriptorSet();
    }

}
