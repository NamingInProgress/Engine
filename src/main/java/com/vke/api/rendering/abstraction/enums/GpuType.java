package com.vke.api.rendering.abstraction.enums;

import com.vke.api.rendering.abstraction.IntEnum;
import org.lwjgl.vulkan.VK14;

public enum GpuType implements IntEnum {

    DISCRETE(VK14.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU),
    INTEGRATED(VK14.VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU),
    VIRTUAL(VK14.VK_PHYSICAL_DEVICE_TYPE_VIRTUAL_GPU),
    CPU(VK14.VK_PHYSICAL_DEVICE_TYPE_CPU),
    OTHER(VK14.VK_PHYSICAL_DEVICE_TYPE_OTHER);

    private final int vkHandle;

    GpuType(int vkHandle) {
        this.vkHandle = vkHandle;
    }

    @Override
    public int getVkHandle() {
        return vkHandle;
    }

}
