package com.vke.core.vulkan.buffers.premade;

import com.vke.api.draw.Vertex;
import com.vke.api.rendering.vulkan.buffer.VertexBuffer;
import com.vke.api.rendering.vulkan.buffer.VertexByteSink;
import com.vke.core.rendering.bytesenik.ByteBufferSink;

import java.util.List;

public class StaticVertexBuffer<T extends Vertex> extends VertexBuffer {
    private final T template;
    private VertexByteSink sink;

    public StaticVertexBuffer(T template, List<T> vertices) {
        super(vertices.size(), template.getByteStride());
        this.sink = new ByteBufferSink(data);

        this.template = template;

        for (T v : vertices) {
            putVertex(v);
            elementCount++;
        }
        data.flip();
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
