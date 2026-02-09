package com.vke.core.rendering.vulkan.descriptor;

import com.vke.api.vulkan.VkEnum;
import org.lwjgl.vulkan.VK14;

public enum DescriptorType implements VkEnum {
    CombinedImageSampler(VK14.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER),
    StorageImage(VK14.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE),
    UniformBuffer(VK14.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER),
    StorageBuffer(VK14.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER);

    private final int vk;

    DescriptorType(int vk) {
        this.vk = vk;
    }

    @Override
    public int getVkHandle() {
        return vk;
    }
}
