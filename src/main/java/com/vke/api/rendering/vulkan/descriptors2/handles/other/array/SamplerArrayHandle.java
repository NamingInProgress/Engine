package com.vke.api.rendering.vulkan.descriptors2.handles.other.array;

import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.SamplerBinding;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.core.rendering.vulkan.sampler.VulkanSampler;

public class SamplerArrayHandle extends UniformHandle {

    public final SamplerBinding samplBinding;

    public SamplerArrayHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, SamplerBinding samplBinding) {
        super(group, set, binding, type, samplBinding);
        this.samplBinding = samplBinding;
    }

    public void set(Sampler sampler, int index) {
        this.samplBinding.samplers[index] = (VulkanSampler) sampler;
        setDirty();
    }

}
