package com.vke.core.rendering.reflection2.api;

import com.vke.api.rendering.vulkan.descriptors.types.StructType;
import com.vke.api.rendering.vulkan.pipeline.BaseType;

public class DescriptorResource {
    public System name;
    public int set, binding;

    public int nArrayDim;
    public int[] arrayDim;
    public int arrayStride;

    public StructType struct;
    public int baseTypeRaw;
    public BaseType baseType;
    public int multiWrite;

}
