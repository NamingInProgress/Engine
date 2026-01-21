package com.vke.core.rendering.vulkan;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

import java.nio.ByteBuffer;

public class ImageView {
    private long handle;

    public ImageView(VkImageViewCreateInfo info) {


        MemoryUtil.nmemFree(info.address());
    }

    public static VkImageViewCreateInfo copyCreateInfo(VkImageViewCreateInfo original) {
        int size = VkImageViewCreateInfo.SIZEOF;
        long newAddr = MemoryUtil.nmemCalloc(1, size);
        MemoryUtil.memCopy(original.address(), newAddr, size);
        return VkImageViewCreateInfo.create(newAddr);
    }

    public long getHandle() {
        return handle;
    }
}
