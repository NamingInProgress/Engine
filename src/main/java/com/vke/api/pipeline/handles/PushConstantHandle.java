package com.vke.api.pipeline.handles;

import com.vke.api.abstraction.descriptors.buffer.PackingType;
import com.vke.core.vulkan.buffers.premade.BufferSlice;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class PushConstantHandle extends DataHandle {

    public int size;
    public long offset;
    public ByteBuffer buffer;
    public PackingType packing;

    public void write(Consumer<BufferSlice> consumer) {
        consumer.accept(new BufferSlice(MemoryUtil.memAddress(buffer), offset, size, packing));
    }

}
