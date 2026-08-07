package com.vke.core.rendering.reflection2.api;

import com.vke.api.rendering.vulkan.pipeline.BaseType;
import com.vke.core.rendering.reflection2.SpirvItem;

public class VertexAttributeResource {
    public String name;
    public int location;
    public int stride;
    public BaseType baseType;
    public int vecSize;

    public VertexAttributeResource(SpirvItem item) {
        while (item.type == BaseType.TypePointer || item.type == BaseType.Unknown) {
            item = item.componentType;
        }

        this.name = item.name;
        this.location = item.location;
        if (item.type == BaseType.Array) {
            vecSize = (int) item.scalarBits;
            baseType = item.componentType.type;
            this.stride = (int) ((item.bitWidth / 8) * item.scalarBits);
        } else {
            vecSize = 1;
            baseType = item.type;
            this.stride = (item.bitWidth / 8);
        }
    }
}
