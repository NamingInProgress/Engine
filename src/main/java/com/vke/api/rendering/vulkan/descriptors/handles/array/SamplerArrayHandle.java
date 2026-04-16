package com.vke.api.rendering.vulkan.descriptors.handles.array;

import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.SamplerBinding;
import com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle;
import com.vke.core.vulkan.descriptor.CompiledDescriptorSetLayout;
import com.vke.core.vulkan.descriptor.DescriptorWriter;
import com.vke.core.vulkan.sampler.VulkanSampler;

public class SamplerArrayHandle extends UniformHandle {

    public final SamplerBinding samplerBinding;

    public SamplerArrayHandle(int descriptorSetListIndex, int binding, DescriptorType bindingType, PackingType packingType, CompiledDescriptorSetLayout compiledLayout, SamplerBinding samplerBinding) {
        super(descriptorSetListIndex, binding, bindingType, packingType, compiledLayout);
        this.samplerBinding = samplerBinding;
    }

    public void set(Sampler sampler, int index) {
        this.samplerBinding.samplers[index] = (VulkanSampler) sampler;
    }

    @Override
    public void writeDescriptor(DescriptorWriter writer, long handle) {
        writer.writeSamplers(handle, binding, samplerBinding.samplers);
    }

    @Override
    public <T extends UniformHandle> T copy() {
        return (T) new SamplerArrayHandle(descriptorSetListIndex, binding, bindingType, packingType, compiledLayout, samplerBinding.copy());
    }
}
