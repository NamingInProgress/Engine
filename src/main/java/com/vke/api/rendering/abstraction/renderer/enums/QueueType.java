package com.vke.api.rendering.abstraction.renderer.enums;

import com.vke.api.rendering.abstraction.renderer.IntEnum;
import org.lwjgl.vulkan.VK14;

public enum QueueType implements IntEnum {
    GRAPHICS(VK14.VK_QUEUE_GRAPHICS_BIT),
    COMPUTE(VK14.VK_QUEUE_COMPUTE_BIT),
    PRESENT(0),
    TRANSFER(VK14.VK_QUEUE_TRANSFER_BIT),
    SPARSE(VK14.VK_QUEUE_SPARSE_BINDING_BIT);

    private final int bit;

    QueueType(int bit) {
        this.bit = bit;
    }

    public static QueueType[] validTypes() {
        return new QueueType[]{ GRAPHICS, COMPUTE, TRANSFER, SPARSE };
    }

    @Override
    public int getIntVal() {
        return this.bit;
    }
}
