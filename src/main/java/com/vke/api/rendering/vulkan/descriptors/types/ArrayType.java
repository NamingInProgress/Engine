package com.vke.api.rendering.vulkan.descriptors.types;

import java.util.Objects;

public class ArrayType extends TypeLayout {

    public int length = -1; // -1 for runtime size arrays
    public long stride;
    public TypeLayout elementType;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ArrayType arrayType = (ArrayType) o;
        return length == arrayType.length && stride == arrayType.stride && Objects.equals(elementType, arrayType.elementType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(length, stride, elementType);
    }
}
