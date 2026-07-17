package com.vke.core.rendering.vulkan.buffers.premade.vbo;

import com.vke.api.rendering.abstraction.draw.Vertex;
import com.vke.api.rendering.vulkan.buffer.VertexBuffer;
import com.vke.api.rendering.abstraction.renderer.data.VertexEncoder;
import com.vke.core.rendering.bytesink.VulkanVertexEncoder;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;

import java.util.List;

public class StaticVertexBuffer<T extends Vertex> extends VertexBuffer {
    private final T template;
    private final VulkanRenderSystem sys;

    public StaticVertexBuffer(VulkanRenderSystem sys, T template, List<T> vertices) {
        super(vertices.size(), template.getByteStride());
        this.sys = sys;

        this.template = template;

        for (T v : vertices) {
            putVertex(v);
            elementCount++;
        }
        data.flip();
    }

    @Override
    protected VertexEncoder generateEncoder() {
        return new VulkanVertexEncoder(sys, data);
    }

    private void putVertex(T v) {
        ensureSpace(1);
        v.putSelf(this.encoder);
    }

    @Override
    public int getByteStride() {
        return template.getByteStride();
    }
}
