package com.vke.core.vulkan.buffers.premade;

import com.vke.api.vulkan.buffer.Vertex;
import com.vke.api.vulkan.buffer.VertexBuffer;

import java.util.List;

public class StaticVertexBuffer<T extends Vertex> extends VertexBuffer {
    private final T template;

    public StaticVertexBuffer(T template, List<T> vertices) {
        super(vertices.size(), template.getByteStride());

        this.template = template;

        for (T v : vertices) {
            elementCount++;
            putVertex(v);
        }
        data.flip();
    }

    private void putVertex(T v) {
        ensureSpace(1);
        v.putSelf(data);
    }

    @Override
    public int getByteStride() {
        return template.getByteStride();
    }
}
