package com.vke.api.rendering.abstraction.renderer.data;

import com.vke.api.rendering.abstraction.draw.QuadTexture;
import com.vke.api.rendering.abstraction.renderer.enums.buffer.MemoryUsage;
import com.vke.api.rendering.abstraction.renderer.enums.texture.*;
import com.vke.core.file.png.Pixels;
import com.vke.core.rendering.vulkan.extent.Extent2D;
import com.vke.core.rendering.vulkan.extent.Extent3D;
import com.vke.utils.io.Disposable;

import java.util.Objects;
import java.util.function.Consumer;

public interface Texture extends Disposable, QuadTexture {

    class TextureDesc {

        public final int width, height, depth;
        public final int mipLevels;
        public final int arrayLayers;

        public final Format format;
        public final ImageUsage usage;
        public final SampleCount samples;
        public final TextureType type;
        public final MemoryUsage memUsage;
        public final ImageTiling tiling;

        public final boolean generateMips;
        public final boolean cubeMap;

        public final Extent3D extent;

        public TextureDesc(int width, int height, int depth, int mipLevels, int arrayLayers, Format format, ImageUsage usage,
                           SampleCount samples, TextureType type, MemoryUsage memUsage, ImageTiling tiling, boolean generateMips) {
            this.width = width;
            this.height = height;
            this.depth = depth;
            this.mipLevels = mipLevels;
            this.arrayLayers = arrayLayers;
            this.format = format;
            this.usage = usage;
            this.samples = samples;
            this.type = type;
            this.memUsage = memUsage;
            this.tiling = tiling;
            this.generateMips = generateMips;
            this.extent = new Extent3D(width, height, depth);
            this.cubeMap = type == TextureType.TEX_CUBE || type == TextureType.TEX_CUBE_ARRAY;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            TextureDesc that = (TextureDesc) o;
            return width == that.width && height == that.height && depth == that.depth && mipLevels == that.mipLevels && arrayLayers == that.arrayLayers && generateMips == that.generateMips && cubeMap == that.cubeMap && format == that.format && Objects.equals(usage, that.usage) && samples == that.samples && type == that.type && Objects.equals(memUsage, that.memUsage) && tiling == that.tiling && Objects.equals(extent, that.extent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(width, height, depth, mipLevels, arrayLayers, format, usage, samples, type, memUsage, tiling, generateMips, cubeMap, extent);
        }

        //        public int width, height, depth = 1;
//        public int mipLevels = 1;
//        public TextureType type;
//        public Format format;
//        public ImageAspect aspect = new ImageAspect(ImageAspect.Bits.COLOR);
//
//        public ImageUsage usage;
//
//        public Extent3D getExtent() {
//            return new Extent3D(width, height, depth);
//        }
//
//        public static TextureDesc tex2D(int width, int height, Format format) {
//            TextureDesc d = new TextureDesc();
//            d.width = width;
//            d.height = height;
//            d.depth = 1;
//            d.type = TextureType.TEX_2D;
//            d.format = format;
//            d.mipLevels = 0; // auto
//            d.usage = new ImageUsage(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.TRANSFER_DST_BIT, ImageUsage.Bits.TRANSFER_SRC_BIT);
//            return d;
//        }
//
//        public static TextureDesc colorAttachment2D(int width, int height, Format format) {
//            TextureDesc d = new TextureDesc();
//            d.width = width;
//            d.height = height;
//            d.type = TextureType.TEX_2D;
//            d.format = format;
//            d.mipLevels = 1;
//            d.usage = new ImageUsage(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.COLOR_ATTACHMENT_BIT, ImageUsage.Bits.TRANSFER_SRC_BIT);
//            return d;
//        }
//
//        public static TextureDesc depthStencilAttachment2D(int width, int height, Format format) {
//            TextureDesc d = new TextureDesc();
//            d.width = width;
//            d.height = height;
//            d.type = TextureType.TEX_2D;
//            d.format = format;
//            d.mipLevels = 1;
//            d.aspect = new ImageAspect(ImageAspect.Bits.DEPTH, ImageAspect.Bits.STENCIL);
//            d.usage = new ImageUsage(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.DEPTH_STENCIL_ATTACHMENT_BIT);
//            return d;
//        }
//
//        public static TextureDesc depthAttachment2D(int width, int height, Format format) {
//            TextureDesc d = new TextureDesc();
//            d.width = width;
//            d.height = height;
//            d.type = TextureType.TEX_2D;
//            d.format = format;
//            d.mipLevels = 1;
//            d.aspect = new ImageAspect(ImageAspect.Bits.DEPTH);
//            d.usage = new ImageUsage(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.DEPTH_STENCIL_ATTACHMENT_BIT);
//            return d;
//        }
//
//        public static TextureDesc cube(int size, Format format) {
//            TextureDesc d = new TextureDesc();
//            d.width = size;
//            d.height = size;
//            d.depth = 1;
//            d.type = TextureType.TEX_CUBE;
//            d.format = format;
//            d.mipLevels = 0; // auto
//            d.usage = new ImageUsage(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.TRANSFER_DST_BIT, ImageUsage.Bits.TRANSFER_SRC_BIT);
//            return d;
//        }
//
//        public static TextureDesc storage2D(int width, int height, Format format) {
//            TextureDesc d = new TextureDesc();
//            d.width = width;
//            d.height = height;
//            d.type = TextureType.TEX_2D;
//            d.format = format;
//            d.mipLevels = 1;
//            d.usage = new ImageUsage(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.TRANSFER_DST_BIT, ImageUsage.Bits.STORAGE_BIT);
//            return d;
//        }
//
//        public static TextureDesc albedo2D(int w, int h) {
//            return tex2D(w, h, Format.RGBA8_SRGB);
//        }
//
//        public static TextureDesc normal2D(int w, int h) {
//            return tex2D(w, h, Format.RGBA8);
//        }
//
//        public static TextureDesc hdrColor2D(int w, int h) {
//            return colorAttachment2D(w, h, Format.RGBA16F);
//        }
//
//        public static TextureDesc shadowMap2D(int w, int h) {
//            return depthStencilAttachment2D(w, h, Format.DEPTH32F);
//        }

        public static TextureDescriptorBuilder builder() { return new TextureDescriptorBuilder(); }
    }

    class TextureDescriptorBuilder {
        private int width, height, depth = 1;
        private int mipLevels = 1;
        private int arrayLayers = 1;

        private Format format;
        private ImageUsage usage;
        private SampleCount samples = SampleCount.X1;
        private TextureType type;
        private MemoryUsage memUsage = MemoryUsage.Bits.GPU_ONLY.into();
        private ImageTiling tiling = ImageTiling.OPTIMAL;

        private boolean generateMips = false;

        public TextureDescriptorBuilder width(int width) {
            this.width = width;
            return this;
        }

        public TextureDescriptorBuilder height(int height) {
            this.height = height;
            return this;
        }

        public TextureDescriptorBuilder depth(int depth) {
            this.depth = depth;
            return this;
        }

        public TextureDescriptorBuilder mipLevels(int mipLevels) {
            this.mipLevels = mipLevels;
            return this;
        }

        public TextureDescriptorBuilder arrayLayers(int arrayLayers) {
            this.arrayLayers = arrayLayers;
            return this;
        }

        public TextureDescriptorBuilder format(Format format) {
            this.format = format;
            return this;
        }

        public TextureDescriptorBuilder usage(ImageUsage usage) {
            this.usage = usage;
            return this;
        }

        public TextureDescriptorBuilder sampleCount(SampleCount samples) {
            this.samples = samples;
            return this;
        }

        public TextureDescriptorBuilder type(TextureType type) {
            this.type = type;
            return this;
        }

        public TextureDescriptorBuilder memoryUsage(MemoryUsage memUsage) {
            this.memUsage = memUsage;
            return this;
        }

        public TextureDescriptorBuilder tiling(ImageTiling tiling) {
            this.tiling = tiling;
            return this;
        }

        public TextureDescriptorBuilder generateMips(boolean generateMips) {
            this.generateMips = generateMips;
            return this;
        }

        public TextureDescriptorBuilder size(Extent2D ext) {
            this.width = ext.width;
            this.height = ext.height;
            return this;
        }

        public TextureDescriptorBuilder size(Extent3D ext) {
            this.width = ext.width;
            this.height = ext.height;
            this.depth = ext.depth;
            return this;
        }

        public TextureDesc build() {
            if (width == 0 || height == 0 || format == null || usage == null || type == null)
                throw new IllegalStateException("Cannot build image without one of these: width, height, format, usage, type");
            return new TextureDesc(width, height, depth, mipLevels, arrayLayers,
                    format, usage, samples, type, memUsage, tiling, generateMips);
        }

    }

    float[] DEFAULT_UVWH = new float[]{ 0, 0, 1, 1 };

    Texture upload(Pixels pixels);

    default int width() { return description().width; }
    default int height() { return description().height; }
    default int depth() { return description().depth; }

    default int width(int mip) { return (int) (description().width / Math.pow(2, mip)); }
    default int height(int mip) { return (int) (description().height / Math.pow(2, mip)); }
    default int depth(int mip) { return (int) (description().depth / Math.pow(2, mip)); }

    default int mipLevels() { return description().mipLevels; }
    default int arrayLayers() { return description().arrayLayers; }

    default Format format() { return description().format; }

    ImageView defaultView();
    ImageView getView(Consumer<ImageView.ImageViewDescriptionBuilder> consumer);
    TextureDesc description();

    void useInShader();
    void useAsSrc();
    void useAsDst();

    @Override
    default Texture texture() {
        return this;
    }

    @Override
    default float[] uvFor() { return DEFAULT_UVWH; }
}
