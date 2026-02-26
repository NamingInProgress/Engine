package com.vke.api.rendering.vulkan.descriptors.bindings;

import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.core.vulkan.buffers.MappedBuffer;

public class BufferBinding extends DescriptorBinding {

    public final MappedBuffer buffer;
    public final long bufferSize; // ALIGNED, Used for buffer arrays, if the descriptor is not an array set it to the size of the buffer
    public final int numBuffers;

    public BufferBinding(BindingLayout layout, MappedBuffer buffer, long bufferSize) {
        super(layout);
        this.buffer = buffer;
        this.bufferSize = bufferSize;
        this.numBuffers = (int) (buffer.getSize() / bufferSize);
    }

    /**
     *  layout(set = 0, binding = 0) uniform MyUBOArray {
     *      float x;
     *  } myUboArray[];
     *
     *
     */

}
