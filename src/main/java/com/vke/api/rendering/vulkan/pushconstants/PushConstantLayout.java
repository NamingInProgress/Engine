package com.vke.api.rendering.vulkan.pushconstants;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.types.TypeLayout;

public class PushConstantLayout {

    public String name;
    public long offset;
    public long size;
    public TypeLayout typeLayout;
    public PackingType packingType;

    public PushConstantLayout(String name, long offset, long size, TypeLayout typeLayout, PackingType packingType) {
        this.name = name;
        this.offset = offset;
        this.size = size;
        this.typeLayout = typeLayout;
        this.packingType = packingType;
    }

}
