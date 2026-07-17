package com.vke.api.rendering.vulkan.descriptors.types;

import com.vke.api.rendering.vulkan.descriptors.PrimitiveBaseType;

import java.util.Objects;

public class PrimitiveType extends TypeLayout {

    public PrimitiveBaseType scalarType;
    public int vecSize;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PrimitiveType that = (PrimitiveType) o;
        return vecSize == that.vecSize && scalarType == that.scalarType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(scalarType, vecSize);
    }
}
