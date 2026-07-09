package com.vke.api.rendering.vulkan.descriptors.types;

import java.util.Objects;

public class ArrayType extends TypeLayout {

    public int elementCount;
    public long stride;
    public TypeLayout elementType;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ArrayType arrayType = (ArrayType) o;
        return elementCount == arrayType.elementCount && stride == arrayType.stride && Objects.equals(elementType, arrayType.elementType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementCount, stride, elementType);
    }
}
