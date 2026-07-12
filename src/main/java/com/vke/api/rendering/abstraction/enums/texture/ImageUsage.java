package com.vke.api.rendering.abstraction.enums.texture;

import com.vke.api.rendering.abstraction.IntBitEnum;
import com.vke.api.rendering.abstraction.IntEnum;
import org.lwjgl.vulkan.VK14;

public class ImageUsage implements IntBitEnum<ImageUsage, ImageUsage.Bits> {

    private int mask;

    public ImageUsage(ImageUsage.Bits... bits) {
        or(bits);
    }

    @Override
    public ImageUsage or(ImageUsage.Bits... bits) {
        for (ImageUsage.Bits bit : bits) {
            mask |= bit.getVkHandle();
        }
        return this;
    }

    public static ImageUsage of(int mask) {
        ImageUsage self = new ImageUsage();
        self.mask = mask;
        return self;
    }

    @Override
    public int getVkHandle() {
        return mask;
    }

    public enum Bits implements IntEnum {

        TRANSFER_SRC_BIT(VK14.VK_IMAGE_USAGE_TRANSFER_SRC_BIT),
        TRANSFER_DST_BIT(VK14.VK_IMAGE_USAGE_TRANSFER_DST_BIT),
        SAMPLED_BIT(VK14.VK_IMAGE_USAGE_SAMPLED_BIT),
        STORAGE_BIT(VK14.VK_IMAGE_USAGE_STORAGE_BIT),
        COLOR_ATTACHMENT_BIT(VK14.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT),
        DEPTH_STENCIL_ATTACHMENT_BIT(VK14.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT),
        TRANSIENT_ATTACHMENT_BIT(VK14.VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT),
        INPUT_ATTACHMENT_BIT(VK14.VK_IMAGE_USAGE_INPUT_ATTACHMENT_BIT);

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
