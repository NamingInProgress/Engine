package com.vke.api.rendering.vulkan.descriptors.bindings;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.core.vulkan.buffers.MappedBuffer;

public class BufferBinding extends DescriptorBinding {

    public final MappedBuffer buffer;
    public final long singleBufferSize; // ALIGNED, Used for buffer arrays, if the descriptor is not an array set it to the size of the buffer
    public final int numBuffers;
    public final PackingType packingType;

    public BufferBinding(BindingLayout layout, MappedBuffer buffer, long singleBufferSize, PackingType packingType) {
        super(layout);
        this.buffer = buffer;
        this.singleBufferSize = singleBufferSize;
        this.numBuffers = (int) (buffer.getSize() / singleBufferSize);
        this.packingType = packingType;
    }

}
