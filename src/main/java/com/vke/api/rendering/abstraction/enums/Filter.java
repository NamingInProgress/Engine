package com.vke.api.rendering.abstraction.enums;

import com.vke.api.rendering.abstraction.IntEnum;
import org.lwjgl.vulkan.VK14;

public enum Filter implements IntEnum {

    NEAREST(VK14.VK_FILTER_NEAREST),
    LINEAR(VK14.VK_FILTER_LINEAR);

    private final int vkHandle;

    Filter(int vkHandle) {
        this.vkHandle = vkHandle;
    }

    @Override
    public int getVkHandle() {
        return this.vkHandle;
    }
}
