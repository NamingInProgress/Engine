package com.vke.core.vulkan.buffers.premade.vbo;

import com.vke.api.draw.Vertex;
import com.vke.api.rendering.vulkan.buffer.VertexBuffer;
import com.vke.api.rendering.vulkan.buffer.VertexByteSink;
import com.vke.api.utils.AlignedByteBuffer;
import com.vke.core.rendering.bytesenik.AlignedBBSink;
import com.vke.core.rendering.bytesenik.ByteBufferSink;

public class DynamicVertexBuffer<T extends Vertex> extends VertexBuffer {
    private final T template;

    public DynamicVertexBuffer(T template, int expectedVertexCount) {
        super(expectedVertexCount * template.getByteStride());
        this.template = template;
    }

    public void clear() {
        elementCount = 0;
        data.clear();
    }

    public void putVertex(T vertex) {
        ensureSpace(1);
        elementCount++;
        vertex.putSelf(this.sink);
    }

    public void putVertices(T... vertices) {
        ensureSpace(vertices.length);
        elementCount += vertices.length;
        for (T vertex : vertices) {
            vertex.putSelf(this.sink);
        }
    }

    @Override
    public int getByteStride() {
        return template.getByteStride();
    }

    @Override
    protected VertexByteSink generateSink() {
        return new ByteBufferSink(data);
    }

}
