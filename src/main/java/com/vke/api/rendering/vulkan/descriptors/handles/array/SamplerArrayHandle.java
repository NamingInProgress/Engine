package com.vke.api.rendering.vulkan.descriptors.handles.array;

import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.SamplerBinding;
import com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle;
import com.vke.core.vulkan.descriptor.DescriptorWriter;
import com.vke.core.vulkan.sampler.VulkanSampler;

public class SamplerArrayHandle extends UniformHandle {

    public final SamplerBinding samplerBinding;

    public SamplerArrayHandle(long setHandle, int binding, DescriptorType bindingType, PackingType packingType, SamplerBinding samplerBinding) {
        super(setHandle, binding, bindingType, packingType);
        this.samplerBinding = samplerBinding;
    }

    public void set(Sampler sampler, int index) {
        this.samplerBinding.samplers[index] = (VulkanSampler) sampler;
    }

    @Override
    public void writeDescriptor(DescriptorWriter writer) {
        writer.writeSamplers(setHandle, binding, samplerBinding.samplers);
    }
}
