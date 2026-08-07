package com.vke.core.rendering.reflection2.api;

import com.vke.core.rendering.reflection2.StorageClass;

public enum DescriptorCategory {
    UNKNOWN,
    UNIFORM_BUFFER,
    STORAGE_BUFFER,
    STORAGE_IMAGE,
    ATOMIC_COUNTER,
    PUSH_CONSTANT,
    ACCELERATION_STRUCTURE,
    SHADER_RECORD_BUFFER;

    public static DescriptorCategory fromStorageClass(int storageClass) {
        return switch (storageClass) {
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
}
