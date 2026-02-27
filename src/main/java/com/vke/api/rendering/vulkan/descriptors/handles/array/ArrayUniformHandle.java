package com.vke.api.rendering.vulkan.descriptors.handles.array;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle;

public abstract class ArrayUniformHandle extends UniformHandle {

    public final int arraySize;

    public ArrayUniformHandle(long setHandle, int binding, DescriptorType bindingType, PackingType packingType, int arraySize) {
        super(setHandle, binding, bindingType, packingType);
        this.arraySize = arraySize;
    }

}
