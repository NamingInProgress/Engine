package com.vke.api.rendering.abstraction.renderer.enums.buffer;

import com.vke.api.rendering.abstraction.renderer.IntBitEnum;
import com.vke.api.rendering.abstraction.renderer.IntEnum;
import org.lwjgl.vulkan.VK14;

public class BufferUsage implements IntBitEnum<BufferUsage, BufferUsage.Bits> {

    private int mask;

    public BufferUsage(Bits... bits) {
        or(bits);
    }

    @Override
    public BufferUsage or(Bits... flags) {
        for (Bits bit : flags) {
            mask |= bit.getVkHandle();
        }
        return this;
    }

    @Override
    public int getVkHandle() {
        return this.mask;
    }

    public enum Bits implements IntEnum {

        TRANSFER_SRC(VK14.VK_BUFFER_USAGE_TRANSFER_SRC_BIT),
        TRANSFER_DST(VK14.VK_BUFFER_USAGE_TRANSFER_DST_BIT),
        UNIFORM_TEXEL_BUFFER(VK14.VK_BUFFER_USAGE_UNIFORM_TEXEL_BUFFER_BIT),
        STORAGE_TEXEL_BUFFER(VK14.VK_BUFFER_USAGE_STORAGE_TEXEL_BUFFER_BIT),
        UBO(VK14.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT),
        SSBO(VK14.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT),
        IBO(VK14.VK_BUFFER_USAGE_INDEX_BUFFER_BIT),
        VBO(VK14.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT),
        INDIRECT_BUFFER(VK14.VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT),
        SHADER_DEVICE_ADDRESS(VK14.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT);

        private final int vkHandle;

        Bits(int vkHandle) {
            this.vkHandle = vkHandle;
        }

        @Override
        public int getVkHandle() {
            return vkHandle;
        }

        public BufferUsage into() {
            return new BufferUsage(this);
        }
    }

}
