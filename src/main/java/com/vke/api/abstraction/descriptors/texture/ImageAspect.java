package com.vke.api.abstraction.descriptors.texture;

import com.vke.api.abstraction.IntBitEnum;
import com.vke.api.abstraction.IntEnum;
import org.lwjgl.vulkan.VK14;

public class ImageAspect implements IntBitEnum<ImageAspect, ImageAspect.Bits> {

    private int mask;

    public ImageAspect(Bits... bits) {
        or(bits);
    }

    public static ImageAspect fromMask(int mask) {
        ImageAspect self = new ImageAspect();
        self.mask = mask;
        return self;
    }

    @Override
    public ImageAspect or(Bits... bits) {
        for (Bits bit : bits) {
            mask |= bit.getVkHandle();
        }
        return this;
    }

    @Override
    public int getVkHandle() {
        return this.mask;
    }

    public enum Bits implements IntEnum {

        COLOR(VK14.VK_IMAGE_ASPECT_COLOR_BIT),
        DEPTH(VK14.VK_IMAGE_ASPECT_DEPTH_BIT),
        STENCIL(VK14.VK_IMAGE_ASPECT_STENCIL_BIT),
        METADATA(VK14.VK_IMAGE_ASPECT_METADATA_BIT);

        private final int vkHandle;

        Bits(int vkHandle) {
            this.vkHandle = vkHandle;
        }

        @Override
        public int getVkHandle() {
            return vkHandle;
        }
    }

}
