package com.vke.core.vulkan.buffers.premade.ibo;

import java.nio.IntBuffer;

public class DynamicIndexBuffer extends IndexBuffer {

    public DynamicIndexBuffer(int baseCap) {
        super(baseCap);
    }

    public IntBuffer getData() {
        return this.data;
    }

    @Override
    public void put(int... indices) {
        ensureSpace(indices.length);
        data.put(indices);
        elementCount += indices.length;
    }

}
