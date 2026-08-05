package com.vke.core.rendering.bytesink;

import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.data.RenderingEncoder;
import com.vke.api.rendering.pbr.Material;
import com.vke.core.rendering.vulkan.buffers.premade.slice.BufferSlice;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

public class VulkanRenderingEncoder implements RenderingEncoder {

    private final ByteBuffer buffer;
    private final VulkanRenderSystem vkCtx;

    public VulkanRenderingEncoder(VulkanRenderSystem vkCtx, ByteBuffer buffer) {
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
    public void sampler2D(@Nullable Texture texture) {
        buffer.putInt(vkCtx.textureManager().texture(texture));
    }

    @Override
    public void material(@Nullable Material material) {
        buffer.putInt(vkCtx.materialManager().material(material));
    }
}
