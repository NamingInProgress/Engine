package com.vke.core.rendering.bytesenik;

import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.vulkan.buffer.VertexEcoder;
import com.vke.core.vulkan.service.VulkanRenderer;

import java.nio.ByteBuffer;

public class VulkanVertexEncoder implements VertexEcoder {

    private final ByteBuffer buffer;

    public VulkanVertexEncoder(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void int1(int x) {
        buffer.putInt(x);
    }

    @Override
    public void float1(float x) {
        buffer.putFloat(x);
    }

    @Override
    public void uint1(int x) {
        buffer.putInt(x);
    }

    @Override
    public void double1(double x) {
        buffer.putDouble(x);
    }

    @Override
    public void sampler2D(Texture texture) {
        //buffer.putInt(context.)
    }
}
