package com.vke.core.rendering.reflection2.api;

import com.vke.api.rendering.vulkan.descriptors.types.StructType;
import com.vke.api.rendering.vulkan.pipeline.BaseType;

public class PushConstantsResource {
    public String name;
    public StructType struct;
    public int baseTypeRaw;
    public BaseType baseType;
    public int size;

}
