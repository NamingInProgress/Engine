package com.vke.api.abstraction.descriptors.texture;

import com.vke.api.abstraction.IntBitEnum;
import com.vke.api.abstraction.IntEnum;
import org.lwjgl.vulkan.VK14;

public class TextureAspect implements IntBitEnum<TextureAspect, TextureAspect.Bits> {

    private int mask;

    public TextureAspect(Bits... bits) {
        or(bits);
    }

    @Override
    public TextureAspect or(Bits... bits) {
        for (Bits bit : bits) {
            mask |= bit.getVkHandle();
        }
        return this;
    }

    @Override
    public int getVkHandle() {
        return mask;
    }

    public enum Bits implements IntEnum {

        ASPECT_NONE(VK14.VK_IMAGE_ASPECT_NONE),
        ASPECT_COLOR_BIT(VK14.VK_IMAGE_ASPECT_COLOR_BIT),
        ASPECT_DEPTH_BIT(VK14.VK_IMAGE_ASPECT_DEPTH_BIT),
        ASPECT_STENCIL_BIT(VK14.VK_IMAGE_ASPECT_STENCIL_BIT),
        ASPECT_METADATA_BIT(VK14.VK_IMAGE_ASPECT_METADATA_BIT),
        ASPECT_PLANE_0_BIT(VK14.VK_IMAGE_ASPECT_PLANE_0_BIT),
        ASPECT_PLANE_1_BIT(VK14.VK_IMAGE_ASPECT_PLANE_1_BIT),
        ASPECT_PLANE_2_BIT(VK14.VK_IMAGE_ASPECT_PLANE_2_BIT);

        private final int vkHandle;

        Bits(int vkHandle) {
            this.vkHandle = vkHandle;
        }

        @Override
        public int getVkHandle() {
            return this.vkHandle;
        }

    }

}
