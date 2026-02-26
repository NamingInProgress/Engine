package com.vke.api.rendering.abstraction.enums.texture;

import com.vke.api.rendering.abstraction.IntEnum;
import org.lwjgl.vulkan.VK14;

public enum TextureType implements IntEnum {

    TEX_1D(VK14.VK_IMAGE_VIEW_TYPE_1D),
    TEX_2D(VK14.VK_IMAGE_VIEW_TYPE_2D),
    TEX_3D(VK14.VK_IMAGE_VIEW_TYPE_3D),

    TEX_1D_ARRAY(VK14.VK_IMAGE_VIEW_TYPE_1D_ARRAY),
    TEX_2D_ARRAY(VK14.VK_IMAGE_VIEW_TYPE_2D_ARRAY),

    TEX_CUBE(VK14.VK_IMAGE_VIEW_TYPE_CUBE),
    TEX_CUBE_ARRAY(VK14.VK_IMAGE_VIEW_TYPE_CUBE_ARRAY);

    private final int vkHandle;

    TextureType(int vkHandle) {
        this.vkHandle = vkHandle;
    }

    @Override
    public int getVkHandle() {
        return this.vkHandle;
    }
}
