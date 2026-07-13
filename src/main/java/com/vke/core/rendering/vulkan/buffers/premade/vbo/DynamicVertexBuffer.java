package com.vke.core.rendering.vulkan.buffers.premade.vbo;

import com.vke.api.draw.Vertex;
import com.vke.api.rendering.vulkan.buffer.VertexBuffer;
import com.vke.api.rendering.abstraction.data.VertexEncoder;
import com.vke.core.rendering.bytesenik.VulkanVertexEncoder;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;

public class DynamicVertexBuffer<T extends Vertex> extends VertexBuffer {
    private final T _template;
    private final VulkanRenderSystem vkCtx;

    public DynamicVertexBuffer(VulkanRenderSystem vkCtx, T template, int expectedVertexCount) {
        super(expectedVertexCount, false);
        this._template = template;
        this.vkCtx = vkCtx;
        this.alloc(expectedVertexCount * getByteStride());
        this.encoder = generateEncoder();
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
        vertex.putSelf(this.encoder);
    }

    public void putVertices(T... vertices) {
        ensureSpace(vertices.length);
        elementCount += vertices.length;
        for (T vertex : vertices) {
            vertex.putSelf(this.encoder);
        }
    }

    @Override
    public int getByteStride() {
        return _template.getByteStride();
    }

    @Override
    protected VertexEncoder generateEncoder() {
        return new VulkanVertexEncoder(vkCtx, data);
    }

}
