package com.vke.api.rendering.vulkan.descriptors.handles.single;

import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.SamplerBinding;
import com.vke.api.rendering.vulkan.descriptors.handles.array.SamplerArrayHandle;

public class SamplerHandle extends SamplerArrayHandle {

    public final int index; // 0 if not in an array

    public SamplerHandle(long setHandle, int binding, DescriptorType bindingType, PackingType packingType, SamplerBinding samplerBinding, int index) {
        super(setHandle, binding, bindingType, packingType, samplerBinding);
        this.index = index;
    }

    public void set(Sampler sampler) {
        this.set(sampler, index);
    }

}
