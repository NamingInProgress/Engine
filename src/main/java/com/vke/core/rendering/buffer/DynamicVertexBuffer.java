package com.vke.core.rendering.buffer;

import com.vke.api.vulkan.buffer.Vertex;
import com.vke.api.vulkan.buffer.VertexBuffer;

public class DynamicVertexBuffer<T extends Vertex> extends VertexBuffer {
    private final T template;

    public DynamicVertexBuffer(T template) {
        super(99);
        this.template = template;
    }

    public void clear() {
        elementCount = 0;
        data.clear();
    }

    public void putVertex(T vertex) {
        ensureSpace(1);
        elementCount++;
        vertex.putSelf(data);
    }

    public void putVertices(T... vertices) {
        ensureSpace(vertices.length);
        elementCount += vertices.length;
        for (T vertex : vertices) {
            vertex.putSelf(data);
        }
    }

    @Override
    public int getByteStride() {
        return template.getByteStride();
    }
}
