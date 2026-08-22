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
        this.name = item.name;
        this.location = item.location;

        while (item.type == BaseType.TypePointer || item.type == BaseType.Unknown) {
            item = item.componentType;
        }

        if (item.type == BaseType.Array) {
            vecSize = (int) item.scalarBits;
            var inner = item.componentType;
            while (inner.type == BaseType.TypePointer || inner.type == BaseType.Unknown) {
                inner = inner.componentType;
            }
            baseType = inner.type;
            this.stride = (int) ((inner.bitWidth / 8) * item.scalarBits);
        } else {
            vecSize = 1;
            baseType = item.type;
            this.stride = (item.bitWidth / 8);
        }
    }
}
