package com.vke.core.rendering.bytesink;

import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.data.VertexEncoder;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;

import java.nio.ByteBuffer;

public class VulkanVertexEncoder implements VertexEncoder {

    private final ByteBuffer buffer;
    private final VulkanRenderSystem vkCtx;

    public VulkanVertexEncoder(VulkanRenderSystem vkCtx, ByteBuffer buffer) {
        this.buffer = buffer;
        this.vkCtx = vkCtx;
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
        buffer.putInt(vkCtx.textureManager().texture(texture));
    }
}
