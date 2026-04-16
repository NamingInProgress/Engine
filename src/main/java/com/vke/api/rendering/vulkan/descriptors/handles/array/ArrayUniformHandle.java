package com.vke.api.rendering.vulkan.descriptors.handles.array;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle;
import com.vke.core.vulkan.descriptor.CompiledDescriptorSetLayout;

public abstract class ArrayUniformHandle extends UniformHandle {

    public final int arraySize;

    public ArrayUniformHandle(int descriptorSetListIndex, int binding, DescriptorType bindingType, PackingType packingType, int arraySize, CompiledDescriptorSetLayout compiledLayout) {
        super(descriptorSetListIndex, binding, bindingType, packingType, compiledLayout);
        this.arraySize = arraySize;
    }

}
