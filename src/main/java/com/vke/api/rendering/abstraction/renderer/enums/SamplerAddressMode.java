package com.vke.api.rendering.abstraction.renderer.enums;

import com.vke.api.rendering.abstraction.renderer.IntEnum;
import org.lwjgl.vulkan.VK14;

public enum SamplerAddressMode implements IntEnum {
    CLAMP_TO_BORDER(VK14.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER),
    CLAMP_TO_EDGE(VK14.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE),
    REPEAT(VK14.VK_SAMPLER_ADDRESS_MODE_REPEAT),
    MIRROR_REPEAT(VK14.VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT),
    MIRROR_CLAMP_TO_EDGE(VK14.VK_SAMPLER_ADDRESS_MODE_MIRROR_CLAMP_TO_EDGE);

    private final int vk;

    SamplerAddressMode(int vk) {
        this.vk = vk;
    }

    @Override
    public int getIntVal() {
        return vk;
    }
}
