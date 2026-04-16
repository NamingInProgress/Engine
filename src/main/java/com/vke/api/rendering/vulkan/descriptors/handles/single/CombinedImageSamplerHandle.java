package com.vke.api.rendering.vulkan.descriptors.handles.single;

import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.CombinedImageSamplerBinding;
import com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle;
import com.vke.api.rendering.vulkan.descriptors.handles.array.CombinedImageSamplerArrayHandle;
import com.vke.core.vulkan.descriptor.CompiledDescriptorSetLayout;

public class CombinedImageSamplerHandle extends CombinedImageSamplerArrayHandle {

    public final int index; // 0 if single sampler

    public CombinedImageSamplerHandle(int descriptorSetListIndex, int binding, DescriptorType bindingType, PackingType packingType, CompiledDescriptorSetLayout compiledLayout, CombinedImageSamplerBinding cisBinding, int index) {
        super(descriptorSetListIndex, binding, bindingType, packingType, compiledLayout, cisBinding);
        this.index = index;
    }

    public void set(Texture texture, Sampler sampler) {
        this.set(texture, sampler, index);
    }

    @Override
    public <T extends UniformHandle> T copy() {
        return (T) new CombinedImageSamplerHandle(descriptorSetListIndex, binding, bindingType, packingType, compiledLayout, cisBinding.copy(), index);
    }
}
