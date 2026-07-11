package com.vke.api.rendering.abstraction.enums.texture;

import com.vke.api.rendering.abstraction.IntEnum;
import org.lwjgl.vulkan.VK14;

public enum ImageTiling implements IntEnum {

    OPTIMAL(VK14.VK_IMAGE_TILING_OPTIMAL),
    LINEAR(VK14.VK_IMAGE_TILING_LINEAR);

    private final int vkHandle;

    ImageTiling(int vkHandle) {
        this.vkHandle = vkHandle;
    }

    @Override
    public int getVkHandle() {
        return 0;
    }
}
