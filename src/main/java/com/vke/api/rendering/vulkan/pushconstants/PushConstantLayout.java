package com.vke.api.rendering.vulkan.pushconstants;

import com.vke.api.rendering.abstraction.renderer.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.types.TypeLayout;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PushConstantLayout that = (PushConstantLayout) o;
        return offset == that.offset && size == that.size && Objects.equals(name, that.name) && Objects.equals(typeLayout, that.typeLayout) && packingType == that.packingType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, offset, size, typeLayout, packingType);
    }
}
