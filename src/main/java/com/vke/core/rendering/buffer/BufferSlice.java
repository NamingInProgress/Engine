package com.vke.core.rendering.buffer;

import com.vke.core.rendering.vulkan.buffer.MappedBuffer;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class BufferSlice {
    private final MappedBuffer mappedBuffer;
    private final long offset;
    private final int length;

    public BufferSlice(MappedBuffer mappedBuffer, long offset, int length) {
        this.mappedBuffer = mappedBuffer;
        this.offset = offset;
        this.length = length;
    }

    public void write(Consumer<ByteBuffer> consumer) {
        ByteBuffer slice = MemoryUtil.memAlloc(length);
        long address = MemoryUtil.memAddress(slice);
        consumer.accept(slice);
        mappedBuffer.write(address, length);
        MemoryUtil.memFree(slice);
    }
}
