package com.vke.core.rendering.reflection2.api;

import com.vke.api.rendering.vulkan.pipeline.BaseType;
import com.vke.core.rendering.reflection2.SpirvItem;
import com.vke.core.rendering.reflection2.StorageClass;

public enum DescriptorCategory {
    UNKNOWN,
    UNIFORM_BUFFER,
    UNIFORM_CONSTANT,
    STORAGE_BUFFER,
    STORAGE_IMAGE,
    SAMPLED_IMAGE,
    SEPARATE_SAMPLER,
    SEPARATE_IMAGE,
    ATOMIC_COUNTER,
    PUSH_CONSTANT,
    ACCELERATION_STRUCTURE,
    RAY_QUERY,
    SHADER_RECORD_BUFFER;

    public static DescriptorCategory fromStorageClass(int storageClass) {
        return switch (storageClass) {
            case StorageClass.UNIFORM_CONSTANT -> UNIFORM_CONSTANT;
            case StorageClass.UNIFORM -> UNIFORM_BUFFER;
            case StorageClass.STORAGE_BUFFER,
                 StorageClass.PHYSICAL_STORAGE_BUFFER -> STORAGE_BUFFER;
            case StorageClass.IMAGE -> STORAGE_IMAGE;
            case StorageClass.ATOMIC_COUNTER -> ATOMIC_COUNTER;
            case StorageClass.PUSH_CONSTANT -> PUSH_CONSTANT;
            case StorageClass.SHADER_RECORD_BUFFER_KHR -> SHADER_RECORD_BUFFER;
            default -> UNKNOWN;
        };
    }

    public static DescriptorCategory fromStorageClassOrType(int storageClass, SpirvItem item) {
        return switch (item.type) {
            case BaseType.Image -> DescriptorCategory.SEPARATE_IMAGE;
            case BaseType.Sampler -> DescriptorCategory.SEPARATE_SAMPLER;
            case BaseType.SampledImage -> DescriptorCategory.SAMPLED_IMAGE;
            case BaseType.Array -> fromStorageClassOrType(storageClass, item.componentType);
            default -> fromStorageClass(storageClass);
        };
    }
}
