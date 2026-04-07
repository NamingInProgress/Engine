package com.vke.api.rendering.vulkan.descriptors.handles.single;

import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.CombinedImageSamplerBinding;
import com.vke.api.rendering.vulkan.descriptors.handles.array.CombinedImageSamplerArrayHandle;

public class CombinedImageSamplerHandle extends CombinedImageSamplerArrayHandle {

    public final int index; // 0 if single sampler

    public CombinedImageSamplerHandle(long setHandle, int binding, DescriptorType bindingType, PackingType packingType, CombinedImageSamplerBinding cisBinding, int index) {
        super(setHandle, binding, bindingType, packingType, cisBinding);
        this.index = index;
    }

    public void set(Texture texture, Sampler sampler) {
        this.set(texture, sampler, index);
    }

}
