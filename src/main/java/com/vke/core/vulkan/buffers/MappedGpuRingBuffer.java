package com.vke.core.vulkan.buffers;

import com.vke.api.rendering.abstraction.enums.buffer.BufferUsage;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import org.lwjgl.system.MemoryUtil;

public class MappedGpuRingBuffer extends MappedBuffer {

    private final long singleSize;
    private final int framesInFlight;

    private int frameIndex;

    public MappedGpuRingBuffer(VKEngine engine, VulkanRenderDevice device, long singleSize, int framesInFlight, BufferUsage usage, int... flags) {
        super(engine, device, singleSize * framesInFlight, usage, flags);
        this.singleSize = singleSize;
        this.framesInFlight = framesInFlight;
    }

    public long getOffset() { return (frameIndex % framesInFlight) * singleSize; }

    @Override
    public void write(long srcAddress, long offset, long numBytes) {
        if (numBytes + offset > singleSize) {
            throw new IllegalArgumentException("Write exceeds buffer size");
        }

        long frameOffset = (frameIndex % framesInFlight) * singleSize;
        MemoryUtil.memCopy(srcAddress, mappedAddress + frameOffset + offset, numBytes);
        frameIndex = (frameIndex + 1) % framesInFlight;
    }

}
