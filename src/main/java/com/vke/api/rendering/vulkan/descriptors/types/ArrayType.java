package com.vke.api.rendering.vulkan.descriptors.types;

public class ArrayType extends TypeLayout {

    public int length = -1; // -1 for runtime size arrays
    public long stride;
    public TypeLayout elementType;

}
