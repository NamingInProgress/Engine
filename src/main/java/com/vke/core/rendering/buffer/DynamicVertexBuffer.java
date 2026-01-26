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
        vertexCount = 0;
        data.clear();
    }

    public void putVertex(T vertex) {
        ensureSpaceForVertices(1);
        vertexCount++;
        vertex.putSelf(data);
    }

    public void putVertices(T... vertices) {
        ensureSpaceForVertices(vertices.length);
        vertexCount += vertices.length;
        for (T vertex : vertices) {
            vertex.putSelf(data);
        }
    }

    @Override
    public int getByteStride() {
        return template.getByteStride();
    }
}
