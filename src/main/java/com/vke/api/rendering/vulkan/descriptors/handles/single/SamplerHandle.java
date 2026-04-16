package com.vke.api.rendering.vulkan.descriptors.handles.single;

import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.SamplerBinding;
import com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle;
import com.vke.api.rendering.vulkan.descriptors.handles.array.SamplerArrayHandle;
import com.vke.core.vulkan.descriptor.CompiledDescriptorSetLayout;

public class SamplerHandle extends SamplerArrayHandle {

    public final int index; // 0 if not in an array

    public SamplerHandle(int descriptorSetListIndex, int binding, DescriptorType bindingType, PackingType packingType, CompiledDescriptorSetLayout compiledLayout, SamplerBinding samplerBinding, int index) {
        super(descriptorSetListIndex, binding, bindingType, packingType, compiledLayout, samplerBinding);
        this.index = index;
    }

    public void set(Sampler sampler) {
        this.set(sampler, index);
    }

    @Override
    public <T extends UniformHandle> T copy() {
        return (T) new SamplerHandle(descriptorSetListIndex, binding, bindingType, packingType, compiledLayout, samplerBinding.copy(), index);
    }
}
