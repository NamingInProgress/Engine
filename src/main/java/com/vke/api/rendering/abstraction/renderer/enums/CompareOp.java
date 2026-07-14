package com.vke.api.rendering.abstraction.renderer.enums;

import com.vke.api.rendering.abstraction.renderer.IntEnum;
import com.vke.utils.iter.helpers.Option;
import org.lwjgl.vulkan.VK14;

public enum CompareOp implements IntEnum {

    NEVER(VK14.VK_COMPARE_OP_NEVER),
    LESS(VK14.VK_COMPARE_OP_LESS),
    EQUAL(VK14.VK_COMPARE_OP_EQUAL),
    LEQUAL(VK14.VK_COMPARE_OP_LESS_OR_EQUAL),
    GREATER(VK14.VK_COMPARE_OP_GREATER),
    NOT_EQUAL(VK14.VK_COMPARE_OP_NOT_EQUAL),
    GEQUAL(VK14.VK_COMPARE_OP_GREATER_OR_EQUAL),
    ALWAYS(VK14.VK_COMPARE_OP_ALWAYS);

    private final int vkHandle;

    CompareOp(int vkHandle) {
        this.vkHandle = vkHandle;
    }

    @Override
    public int getVkHandle() {
        return vkHandle;
    }

    public static Option<CompareOp> valueOfOption(String name) {
        return Option.useIfNotFaulty(() -> CompareOp.valueOf(name));
    }

}
