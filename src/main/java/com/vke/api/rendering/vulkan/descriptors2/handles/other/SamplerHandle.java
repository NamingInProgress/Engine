package com.vke.api.rendering.vulkan.descriptors2.handles.other;

import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.SamplerBinding;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.core.rendering.vulkan.sampler.VulkanSampler;

public class SamplerHandle extends UniformHandle {

    public final SamplerBinding samplBinding;
    public final int index;

    public SamplerHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, SamplerBinding samplBinding, int index) {
        super(group, set, binding, type, samplBinding);
        this.samplBinding = samplBinding;
        this.index = index;
    }

    public void set(Sampler sampler) {
        this.samplBinding.samplers[index] = (VulkanSampler) sampler;
        setDirty();
    }
}
