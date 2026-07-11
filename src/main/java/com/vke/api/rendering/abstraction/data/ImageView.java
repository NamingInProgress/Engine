package com.vke.api.rendering.abstraction.data;

import com.vke.api.rendering.abstraction.enums.texture.Format;
import com.vke.api.rendering.abstraction.enums.texture.ImageAspect;
import com.vke.api.rendering.abstraction.enums.texture.TextureType;
import com.vke.utils.io.Disposable;

public interface ImageView extends Disposable {

    class ImageViewDesc {

        public final Texture tex;

        public final TextureType type;
        public final Format format;

        public final int baseMip, baseLayer;
        public int mipCount;
        public int layerCount; // Need to be updated on the backend side for -1 -> REMAINING

        public final ImageAspect aspect;

        public ImageViewDesc(Texture tex, TextureType type, Format format, int baseMip, int mipCount, int baseLayer, int layerCount) {
            this.tex = tex;
            this.type = type;
            this.format = format;
            this.baseMip = baseMip;
            this.baseLayer = baseLayer;
            this.mipCount = mipCount;
            this.layerCount = layerCount;
            if (format.isDepth()) {
                this.aspect = new ImageAspect(ImageAspect.Bits.DEPTH);
                if (format.isStencil()) {
                    this.aspect.or(ImageAspect.Bits.STENCIL);
                }
            } else {
                this.aspect = new ImageAspect(ImageAspect.Bits.COLOR);
            }
        }
    }

    class ImageViewDescriptionBuilder {

        private final Texture tex;

        private TextureType type;
        private Format format;

        private int baseMip = 0, mipCount = -1;
        private int baseLayer = 0, layerCount = -1;

        public ImageViewDescriptionBuilder(Texture parent) {
            this.tex = parent;
        }

        public ImageViewDescriptionBuilder type(TextureType type) {
            this.type = type;
            return this;
        }

        public ImageViewDescriptionBuilder format(Format format) {
            this.format = format;
            return this;
        }

        public ImageViewDescriptionBuilder baseMip(int baseMip) {
            this.baseMip = baseMip;
            return this;
        }

        public ImageViewDescriptionBuilder mipCount(int mipCount) {
            this.mipCount = mipCount;
            return this;
        }

        public ImageViewDescriptionBuilder baseLayer(int baseLayer) {
            this.baseLayer = baseLayer;
            return this;
        }

        public ImageViewDescriptionBuilder layerCount(int layerCount) {
            this.layerCount = layerCount;
            return this;
        }

        public ImageViewDesc build() {
            return new ImageViewDesc(tex, type, format, baseMip, mipCount, baseLayer, layerCount);
        }

    }

    default Texture texture() { return description().tex; }

    default TextureType type() { return description().type; }

    default int baseMipLevel() { return description().baseMip; }
    default int mipLevelCount() { return description().mipCount; }

    default int baseArrayLayer() { return description().baseLayer; }
    default int arrayLayerCount() { return description().layerCount; }

    default Format format() { return description().format; }

    ImageViewDesc description();

}
