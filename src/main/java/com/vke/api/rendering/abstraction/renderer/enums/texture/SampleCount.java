package com.vke.api.rendering.abstraction.renderer.enums.texture;

import com.vke.api.rendering.abstraction.renderer.IntEnum;
import org.lwjgl.vulkan.VK14;

public enum SampleCount implements IntEnum {
    X1(VK14.VK_SAMPLE_COUNT_1_BIT),
    X2(VK14.VK_SAMPLE_COUNT_2_BIT),
    X4(VK14.VK_SAMPLE_COUNT_4_BIT),
    X8(VK14.VK_SAMPLE_COUNT_8_BIT),
    X16(VK14.VK_SAMPLE_COUNT_16_BIT),
    X32(VK14.VK_SAMPLE_COUNT_32_BIT),
    X64(VK14.VK_SAMPLE_COUNT_64_BIT);

    private final int vkHandle;

    SampleCount(int vkHandle) {
        this.vkHandle = vkHandle;
    }

    @Override
    public int getVkHandle() {
        return vkHandle;
    }
}
