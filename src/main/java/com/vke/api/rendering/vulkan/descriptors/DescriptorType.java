package com.vke.api.rendering.vulkan.descriptors;

import com.vke.api.pipeline.BaseType;
import com.vke.api.rendering.abstraction.IntEnum;
import org.lwjgl.vulkan.KHRAccelerationStructure;
import org.lwjgl.vulkan.VK14;

public enum DescriptorType implements IntEnum {

    UNIFORM_BUFFER(VK14.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER),
    STORAGE_BUFFER(VK14.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER),
    UNIFORM_BUFFER_DYNAMIC(VK14.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC),
    STORAGE_BUFFER_DYNAMIC(VK14.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER_DYNAMIC),

    COMBINED_IMAGE_SAMPLER(VK14.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER),
    SAMPLED_IMAGE(VK14.VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE),
    STORAGE_IMAGE(VK14.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE),
    SAMPLER(VK14.VK_DESCRIPTOR_TYPE_SAMPLER),

    ACCELERATION_STRUCTURE(KHRAccelerationStructure.VK_DESCRIPTOR_TYPE_ACCELERATION_STRUCTURE_KHR);

    private final int vkHandle;

    DescriptorType(int vkHandle) {
        this.vkHandle = vkHandle;
    }

    @Override
    public int getVkHandle() {
        return vkHandle;
    }

    public boolean isBuffer() {
        return this == UNIFORM_BUFFER || this == UNIFORM_BUFFER_DYNAMIC || this == STORAGE_BUFFER || this == STORAGE_BUFFER_DYNAMIC;
    }

    public static DescriptorType fromBaseType(BaseType bt, boolean isDynamic) {
        switch (bt) {
            case Struct ->
        }
    }

}
