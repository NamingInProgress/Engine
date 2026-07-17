package com.vke.api.rendering.vulkan.descriptors2.handles.other.array;

import com.vke.api.rendering.abstraction.renderer.data.Sampler;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.other.array.SamplerArrayResource;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.SamplerBinding;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.core.rendering.vulkan.sampler.VulkanSampler;

public class SamplerArrayHandle extends UniformHandle implements SamplerArrayResource {

    public final SamplerBinding samplBinding;

    public SamplerArrayHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, SamplerBinding samplBinding) {
        super(group, set, binding, type, samplBinding);
        this.samplBinding = samplBinding;
    }

    @Override
    public void set(int index, Sampler sampler) {
        this.samplBinding.samplers[index] = (VulkanSampler) sampler;
        setDirty();
    }

    @Override
    public void nextWrite() {
        this.group.getSet(this.set).requestNewDescriptorSet();
    }

}
