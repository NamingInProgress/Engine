package com.vke.core.vulkan.buffers.premade.vbo;

import com.vke.api.draw.Vertex;
import com.vke.api.rendering.vulkan.buffer.VertexBuffer;
import com.vke.api.rendering.vulkan.buffer.VertexEcoder;
import com.vke.core.rendering.bytesenik.VulkanVertexEncoder;

public class DynamicVertexBuffer<T extends Vertex> extends VertexBuffer {
    private final T _template;

    public DynamicVertexBuffer(T template, int expectedVertexCount) {
        super(expectedVertexCount, false);
        this._template = template;
        this.alloc(expectedVertexCount * getByteStride());
        this.sink = generateSink();
    }

    public void clear() {
        elementCount = 0;
        data.clear();
    }

    public void reset() {
        data.position(0);
        elementCount = 0;
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
        return _template.getByteStride();
    }

    @Override
    protected VertexEcoder generateSink() {
        return new VulkanVertexEncoder(data);
    }

}
