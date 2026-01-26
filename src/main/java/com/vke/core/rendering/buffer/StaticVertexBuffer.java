package com.vke.core.rendering.buffer;

import com.vke.api.vulkan.buffer.Vertex;
import com.vke.api.vulkan.buffer.VertexBuffer;

import java.util.List;

public class StaticVertexBuffer<T extends Vertex> extends VertexBuffer {
    private final T template;

    public StaticVertexBuffer(T template, List<T> vertices) {
        super(vertices.size());

        this.template = template;

        for (T v : vertices) {
            vertexCount++;
            putVertex(v);
        }
    }

    private void putVertex(T v) {
        ensureSpaceForVertices(1);
        v.putSelf(data);
    }


    @Override
    public int getByteStride() {
        return template.getByteStride();
    }
}
