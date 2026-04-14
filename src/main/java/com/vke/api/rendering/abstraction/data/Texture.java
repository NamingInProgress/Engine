package com.vke.api.rendering.abstraction.data;

import com.vke.api.draw.QuadTexture;
import com.vke.api.rendering.abstraction.enums.texture.ImageAspect;
import com.vke.api.rendering.abstraction.enums.texture.ImageUsage;
import com.vke.api.rendering.abstraction.enums.texture.Format;
import com.vke.api.rendering.abstraction.enums.texture.TextureType;
import com.vke.core.vulkan.extent.Extent3D;
import com.vke.utils.io.Disposable;

public interface Texture extends Disposable, QuadTexture {

    class TextureDesc {

        public int width, height, depth = 1;
        public int mipLevels = 1;
        public TextureType type;
        public Format format;
        public ImageAspect aspect = new ImageAspect(ImageAspect.Bits.COLOR);

        public ImageUsage usage;

        public Extent3D getExtent() {
            return new Extent3D(width, height, depth);
        }

        public static TextureDesc tex2D(int width, int height, Format format) {
            TextureDesc d = new TextureDesc();
            d.width = width;
            d.height = height;
            d.depth = 1;
            d.type = TextureType.TEX_2D;
            d.format = format;
            d.mipLevels = 0; // auto
            d.usage = new ImageUsage(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.TRANSFER_DST_BIT, ImageUsage.Bits.TRANSFER_SRC_BIT);
            return d;
        }

        public static TextureDesc colorAttachment2D(int width, int height, Format format) {
            TextureDesc d = new TextureDesc();
            d.width = width;
            d.height = height;
            d.type = TextureType.TEX_2D;
            d.format = format;
            d.mipLevels = 1;
            d.usage = new ImageUsage(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.COLOR_ATTACHMENT_BIT, ImageUsage.Bits.TRANSFER_SRC_BIT);
            return d;
        }

        public static TextureDesc depthStencilAttachment2D(int width, int height, Format format) {
            TextureDesc d = new TextureDesc();
            d.width = width;
            d.height = height;
            d.type = TextureType.TEX_2D;
            d.format = format;
            d.mipLevels = 1;
            d.aspect = new ImageAspect(ImageAspect.Bits.DEPTH, ImageAspect.Bits.STENCIL);
            d.usage = new ImageUsage(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.DEPTH_STENCIL_ATTACHMENT_BIT);
            return d;
        }

        public static TextureDesc depthAttachment2D(int width, int height, Format format) {
            TextureDesc d = new TextureDesc();
            d.width = width;
            d.height = height;
            d.type = TextureType.TEX_2D;
            d.format = format;
            d.mipLevels = 1;
            d.aspect = new ImageAspect(ImageAspect.Bits.DEPTH);
            d.usage = new ImageUsage(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.DEPTH_STENCIL_ATTACHMENT_BIT);
            return d;
        }

        public static TextureDesc cube(int size, Format format) {
            TextureDesc d = new TextureDesc();
            d.width = size;
            d.height = size;
            d.depth = 1;
            d.type = TextureType.TEX_CUBE;
            d.format = format;
            d.mipLevels = 0; // auto
            d.usage = new ImageUsage(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.TRANSFER_DST_BIT, ImageUsage.Bits.TRANSFER_SRC_BIT);
            return d;
        }

        public static TextureDesc storage2D(int width, int height, Format format) {
            TextureDesc d = new TextureDesc();
            d.width = width;
            d.height = height;
            d.type = TextureType.TEX_2D;
            d.format = format;
            d.mipLevels = 1;
            d.usage = new ImageUsage(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.TRANSFER_DST_BIT, ImageUsage.Bits.STORAGE_BIT);
            return d;
        }

        public static TextureDesc albedo2D(int w, int h) {
            return tex2D(w, h, Format.RGBA8_SRGB);
        }

        public static TextureDesc normal2D(int w, int h) {
            return tex2D(w, h, Format.RGBA8);
        }

        public static TextureDesc hdrColor2D(int w, int h) {
            return colorAttachment2D(w, h, Format.RGBA16F);
        }

        public static TextureDesc shadowMap2D(int w, int h) {
            return depthStencilAttachment2D(w, h, Format.DEPTH32F);
        }

    }

    float[] DEFAULT_UVWH = new float[]{ 0, 0, 1, 1 };

    int width();
    int height();
    int depth();

    int mipLevels();
    int arrayLayers();

    TextureType type();
    Format format();

    long getHandle();

    @Override
    default Texture texture() {
        return this;
    }

    @Override
    default float[] uvFor() { return DEFAULT_UVWH; }
}
