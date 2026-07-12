package com.vke.api.rendering.vulkan.descriptors2.handles.other;

import com.vke.api.rendering.abstraction.data.ImageView;
import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.CombinedImageSamplerBinding;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.core.vulkan.sampler.VulkanSampler;
import com.vke.core.vulkan.texture.texture2.VulkanImageView;

public class CISHandle extends UniformHandle {

    public final CombinedImageSamplerBinding cisBinding;
    public final int index;

    public CISHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, CombinedImageSamplerBinding cisBinding, int index) {
        super(group, set, binding, type, cisBinding);
        this.cisBinding = cisBinding;
        this.index = index;
    }

    public void set(Texture texture, Sampler sampler) {
        this.cisBinding.views[index] = (VulkanImageView) texture.defaultView();
        this.cisBinding.samplers[index] = (VulkanSampler) sampler;
        setDirty();
    }

    public void set(ImageView view, Sampler sampler) {
        this.cisBinding.views[index] = (VulkanImageView) view;
        this.cisBinding.samplers[index] = (VulkanSampler) sampler;
        setDirty();
    }

}