package com.vke.core.vulkan.buffers;

import com.vke.api.rendering.abstraction.data.GpuBuffer;
import com.vke.core.VKEngine;
import com.vke.api.rendering.abstraction.enums.buffer.BufferUsage;
import com.vke.api.rendering.abstraction.enums.buffer.MemoryUsage;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.utils.io.Disposable;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vma.Vma;

import java.util.Arrays;

public class MappedBuffer implements Disposable {

    public static int counter;

    protected final VulkanGpuBuffer gpuBuffer;
    protected final long mappedAddress;
    protected final long size;

    private int idx = 0;

    public MappedBuffer(
            VKEngine engine,
            VulkanRenderDevice device,
            long size,
            BufferUsage usage,
            int... flags
    ) {
        this.size = size;

        this.gpuBuffer = device.createBuffer(new GpuBuffer.Description(size, usage, MemoryUsage.Bits.AUTO_PREFER_HOST.into(),
                Arrays.stream(flags).reduce(0, (a, b) -> a | b) |
                Vma.VMA_ALLOCATION_CREATE_MAPPED_BIT | Vma.VMA_ALLOCATION_CREATE_HOST_ACCESS_SEQUENTIAL_WRITE_BIT));

        this.mappedAddress = gpuBuffer.getInfo().pMappedData();

        if (mappedAddress == MemoryUtil.NULL) {
            engine.throwException(new IllegalStateException("Could not map buffer data!"), "MappedBuffer");
        }
    }

    public void write(long srcAddress, long offset, long numBytes) {
        if (numBytes > size) {
            throw new IllegalArgumentException("Write exceeds buffer size");
        }

        MemoryUtil.memCopy(srcAddress, mappedAddress + offset, numBytes);
    }

    public long getMappedAddress() {
        return mappedAddress;
    }

    public VulkanGpuBuffer getGpuBuffer() {
        return gpuBuffer;
    }

    public long getSize() { return this.size; }

    @Override
    public void free() {
        gpuBuffer.free();
    }
}
