package com.vke.api.rendering.abstraction.renderer.enums.texture;

import com.vke.api.rendering.abstraction.renderer.IntEnum;
import com.vke.utils.iter.helpers.Option;
import org.lwjgl.vulkan.VK14;

public enum ImageTiling implements IntEnum {

    OPTIMAL(VK14.VK_IMAGE_TILING_OPTIMAL),
    LINEAR(VK14.VK_IMAGE_TILING_LINEAR);

    private final int vkHandle;

    ImageTiling(int vkHandle) {
        this.vkHandle = vkHandle;
    }

    @Override
    public int getIntVal() {
        return vkHandle;
    }

    public static Option<ImageTiling> valueOfOption(String name) {
        return Option.useIfNotFaulty(() -> ImageTiling.valueOf(name));
    }
}
