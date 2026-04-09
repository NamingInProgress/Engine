package com.vke.core.vulkan.buffers.premade;

import com.vke.api.draw.Vertex;
import com.vke.api.rendering.vulkan.buffer.VertexBuffer;
import com.vke.api.rendering.vulkan.buffer.VertexByteSink;
import com.vke.api.utils.AlignedByteBuffer;
import com.vke.core.rendering.bytesenik.AlignedBBSink;
import com.vke.core.rendering.bytesenik.ByteBufferSink;

import java.util.List;

public class StaticVertexBuffer<T extends Vertex> extends VertexBuffer {
    private final T template;
    private final boolean align16;

    public StaticVertexBuffer(T template, List<T> vertices, boolean align16) {
        super(vertices.size(), template.getByteStride());
        this.align16 = align16;

        this.template = template;

        for (T v : vertices) {
            putVertex(v);
            elementCount++;
        }
        data.flip();
    }

    @Override
    protected VertexByteSink generateSink() {
        return align16 ? new AlignedBBSink(new AlignedByteBuffer(data, 16)) : new ByteBufferSink(data);
    }

    private void putVertex(T v) {
        ensureSpace(1);
        v.putSelf(this.sink);
    }

    @Override
    public int getByteStride() {
        return template.getByteStride();
    }
}
