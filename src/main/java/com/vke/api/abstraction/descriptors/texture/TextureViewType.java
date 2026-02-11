package com.vke.api.abstraction.descriptors.texture;

import com.vke.api.abstraction.IntEnum;
import org.lwjgl.vulkan.VK14;

public enum TextureViewType implements IntEnum {

    VIEW_TYPE_1D(VK14.VK_IMAGE_VIEW_TYPE_1D),
    VIEW_TYPE_2D(VK14.VK_IMAGE_VIEW_TYPE_2D),
    VIEW_TYPE_3D(VK14.VK_IMAGE_VIEW_TYPE_3D),
    VIEW_TYPE_CUBE(VK14.VK_IMAGE_VIEW_TYPE_CUBE),
    VIEW_TYPE_1D_ARRAY(VK14.VK_IMAGE_VIEW_TYPE_1D_ARRAY),
    VIEW_TYPE_2D_ARRAY(VK14.VK_IMAGE_VIEW_TYPE_2D_ARRAY),
    VIEW_TYPE_CUBE_ARRAY(VK14.VK_IMAGE_VIEW_TYPE_CUBE_ARRAY);

    private final int vkHandle;

    TextureViewType(int vkHandle) {
        this.vkHandle = vkHandle;
    }

    @Override
    public int getVkHandle() {
        return this.vkHandle;
    }
}
