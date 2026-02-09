package com.vke.core.rendering.vulkan.buffer;

import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.VulkanSetup;
import com.vke.core.rendering.vulkan.mem.GpuBuffer;
import com.vke.utils.Disposable;
import org.lwjgl.system.MemoryUtil;

public class MappedBuffer implements Disposable {
    private final GpuBuffer gpuBuffer;
    private final long mappedAddress;
    private final long size;

    public MappedBuffer(
            VKEngine engine,
            VulkanSetup setup,
            long size,
            GpuBuffer.BufferUsage usage
    ) {
        this.size = size;

        this.gpuBuffer = new GpuBuffer(
                engine,
                setup,
                size,
                usage,
                GpuBuffer.MemoryUsage.Bits.CPU_TO_GPU.into()
        );

        this.mappedAddress = gpuBuffer.getInfo().pMappedData();

        if (mappedAddress == MemoryUtil.NULL) {
            engine.throwException(new IllegalStateException("Could not map buffer data!"), "MappedBuffer");
        }
    }

    public void write(long srcAddress, long numBytes) {
        if (numBytes > size) {
            throw new IllegalArgumentException("Write exceeds buffer size");
        }

        MemoryUtil.memCopy(srcAddress, mappedAddress, numBytes);
    }

    public long getMappedAddress() {
        return mappedAddress;
    }

    public GpuBuffer getGpuBuffer() {
        return gpuBuffer;
    }

    @Override
    public void free() {
        gpuBuffer.free();
    }
}
