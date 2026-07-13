package com.vke.core.vulkan.buffers;

import com.vke.api.rendering.abstraction.enums.buffer.BufferUsage;
import com.vke.core.vulkan.service.VulkanRenderSystem;
import org.lwjgl.system.MemoryUtil;

public class MappedGpuRingBuffer extends MappedBuffer {

    private final long singleSize;
    private final int rings;

    private int frameIndex;

    public MappedGpuRingBuffer(VulkanRenderSystem vkCtx, long singleSize, int rings, BufferUsage usage, int... flags) {
        super(vkCtx, singleSize * rings, usage, flags);
        this.singleSize = singleSize;
        this.rings = rings;
    }

    public long getOffset() { return (frameIndex % rings) * singleSize; }
    public long getLastOffset() { return ((Math.abs(frameIndex - 1)) % rings) * singleSize; }

    @Override
    public void write(long srcAddress, long offset, long numBytes) {
        if (numBytes + offset > singleSize) {
            throw new IllegalArgumentException("Write exceeds buffer size");
        }

        long frameOffset = (frameIndex % rings) * singleSize;
        MemoryUtil.memCopy(srcAddress, mappedAddress + frameOffset + offset, numBytes);
    }

    public void rotate() {
        frameIndex = (frameIndex + 1) % rings;
    }
}
