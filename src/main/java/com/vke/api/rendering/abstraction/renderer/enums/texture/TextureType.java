package com.vke.api.rendering.abstraction.renderer.enums.texture;

import com.vke.api.rendering.abstraction.renderer.IntEnum;
import org.lwjgl.vulkan.VK14;

public enum TextureType implements IntEnum {

    TEX_1D(VK14.VK_IMAGE_TYPE_1D),
    TEX_2D(VK14.VK_IMAGE_TYPE_2D),
    TEX_3D(VK14.VK_IMAGE_TYPE_3D);

    private final int vkHandle;

    TextureType(int vkHandle) {
        this.vkHandle = vkHandle;
    }

    @Override
    public int getIntVal() {
        return this.vkHandle;
    }

    public ImageViewType defaultViewType() {
        return switch (this) {
            case TEX_1D -> ImageViewType.TEX_1D;
            case TEX_2D -> ImageViewType.TEX_2D;
            case TEX_3D -> ImageViewType.TEX_3D;
        };
    }
}
