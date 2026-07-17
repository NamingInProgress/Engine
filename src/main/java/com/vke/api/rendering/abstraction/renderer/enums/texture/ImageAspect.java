package com.vke.api.rendering.abstraction.renderer.enums.texture;

import com.vke.api.rendering.abstraction.renderer.IntBitEnum;
import com.vke.api.rendering.abstraction.renderer.IntEnum;
import org.lwjgl.vulkan.VK14;

public class ImageAspect implements IntBitEnum<ImageAspect, ImageAspect.Bits> {

    public static final ImageAspect AUTO = new ImageAspect(Bits.AUTO);

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

    public ImageAspect resolve(Format format) {
        if (this != AUTO) return this;
        ImageAspect aspect;
        if (format.isDepth()) {
            aspect = new ImageAspect(ImageAspect.Bits.DEPTH);
            if (format.isStencil()) {
                aspect.or(ImageAspect.Bits.STENCIL);
            }
        } else {
            aspect = new ImageAspect(ImageAspect.Bits.COLOR);
        }
        return aspect;
    }

    public enum Bits implements IntEnum {

        COLOR(VK14.VK_IMAGE_ASPECT_COLOR_BIT),
        DEPTH(VK14.VK_IMAGE_ASPECT_DEPTH_BIT),
        STENCIL(VK14.VK_IMAGE_ASPECT_STENCIL_BIT),
        METADATA(VK14.VK_IMAGE_ASPECT_METADATA_BIT),
        AUTO(-1);

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
