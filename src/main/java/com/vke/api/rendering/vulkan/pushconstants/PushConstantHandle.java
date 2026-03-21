package com.vke.api.rendering.vulkan.pushconstants;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.core.vulkan.buffers.premade.slice.BufferSlice;
import com.vke.core.vulkan.buffers.premade.slice.PushConstantBufferSlice;

import java.util.function.Consumer;

public class PushConstantHandle {

    public final long pipelineLayoutHandle; // Replace this cuz its probably not the best thing to use
    public final long bufferAddress;
    public final long size;
    public final long offset;

    public PushConstantHandle(long pipelineLayoutHandle, long bufferAddress, long size, long offset) {
        this.pipelineLayoutHandle = pipelineLayoutHandle;
        this.bufferAddress = bufferAddress;
        this.size = size;
        this.offset = offset;
    }

    public void write(Consumer<BufferSlice> consumer) {
        BufferSlice slice = new PushConstantBufferSlice(bufferAddress, offset, (int) size, PackingType.STD430);
        consumer.accept(slice);
    }

}
