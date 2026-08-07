package com.vke.core.rendering.reflection2.api;

import com.vke.api.rendering.vulkan.descriptors.types.StructType;
import com.vke.api.rendering.vulkan.pipeline.BaseType;
import com.vke.core.rendering.reflection2.SpirvItem;
import com.vke.core.rendering.reflection2.SpirvUtils;

public class PushConstantsResource {
    public String name;
    public StructType struct;
    public BaseType baseType;
    public int size;

    public PushConstantsResource(SpirvItem item) {
        this.name = item.name;
        this.baseType = item.type;
        this.size = SpirvUtils.computeSize(item);
        this.struct = SpirvUtils.createStructType(item);
    }
}