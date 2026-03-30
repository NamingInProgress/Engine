package com.vke.core.vulkan.buffers;

import com.vke.api.rendering.abstraction.data.Buffer;
import com.vke.core.VKEngine;
import com.vke.api.rendering.abstraction.enums.buffer.BufferUsage;
import com.vke.api.rendering.abstraction.enums.buffer.MemoryUsage;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.utils.io.Disposable;
import org.lwjgl.system.MemoryUtil;

public class MappedBuffer implements Disposable {
    private final GpuBuffer gpuBuffer;
    private final long mappedAddress;
    private final long size;

    public MappedBuffer(
            VKEngine engine,
            VulkanRenderDevice device,
            long size,
            BufferUsage usage
    ) {
        this.size = size;

        this.gpuBuffer = device.createBuffer(new Buffer.Description(size, usage, MemoryUsage.Bits.CPU_TO_GPU.into()));

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

    public GpuBuffer getGpuBuffer() {
        return gpuBuffer;
    }

    public long getSize() { return this.size; }

    @Override
    public void free() {
        gpuBuffer.free();
    }
}
