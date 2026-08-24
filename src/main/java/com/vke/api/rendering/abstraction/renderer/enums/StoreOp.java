package com.vke.api.rendering.abstraction.renderer.enums;

import com.vke.api.rendering.abstraction.renderer.IntEnum;
import org.lwjgl.vulkan.VK14;

public enum StoreOp implements IntEnum {

    STORE(VK14.VK_ATTACHMENT_STORE_OP_STORE),
    DONT_CARE(VK14.VK_ATTACHMENT_STORE_OP_DONT_CARE);

    private final int vkHandle;

    StoreOp(int vkHandle) {
        this.vkHandle = vkHandle;
    }

    @Override
    public int getIntVal() {
        return vkHandle;
    }
}
