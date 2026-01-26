package com.vke.api.vulkan;

public interface VkBitEnum<SELF, BITS extends VkEnum> extends VkEnum {
    SELF or(BITS... bits);
}
