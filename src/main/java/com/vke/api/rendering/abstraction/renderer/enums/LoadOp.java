package com.vke.api.rendering.abstraction.renderer.enums;

import com.vke.api.rendering.abstraction.renderer.IntEnum;
import org.lwjgl.vulkan.VK14;

public enum LoadOp implements IntEnum {

    LOAD(VK14.VK_ATTACHMENT_LOAD_OP_LOAD),
    CLEAR(VK14.VK_ATTACHMENT_LOAD_OP_CLEAR),
    DONT_CARE(VK14.VK_ATTACHMENT_LOAD_OP_DONT_CARE);

    private final int vkHandle;

    LoadOp(int vkHandle) {
        this.vkHandle = vkHandle;
    }

    @Override
    public int getVkHandle() {
        return vkHandle;
    }
}
