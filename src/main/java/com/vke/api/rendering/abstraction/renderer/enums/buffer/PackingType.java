package com.vke.api.rendering.abstraction.renderer.enums.buffer;

import com.vke.api.rendering.vulkan.descriptors.DescriptorType;

public enum PackingType {

    STD140,
    STD430;

    public static PackingType fromDescriptorType(DescriptorType descriptorType) {
        if (descriptorType == DescriptorType.UNIFORM_BUFFER || descriptorType == DescriptorType.UNIFORM_BUFFER_DYNAMIC) return STD140;
        if (descriptorType == DescriptorType.STORAGE_BUFFER || descriptorType == DescriptorType.STORAGE_BUFFER_DYNAMIC) return STD430;
        return null;
    }

}
