package com.vke.api.rendering.abstraction.renderer.data;

import com.vke.api.rendering.abstraction.renderer.enums.texture.Format;
import com.vke.api.rendering.abstraction.renderer.enums.texture.ImageAspect;
import com.vke.api.rendering.abstraction.renderer.enums.texture.TextureType;
import com.vke.utils.io.Disposable;

import java.util.Objects;

public interface ImageView extends Disposable {

    class ImageViewDesc {

        public final Texture tex;

        public final TextureType type;
        public final Format format;

        public final int baseMip, baseLayer;
        public int mipCount;
        public int layerCount; // Need to be updated on the backend side for -1 -> REMAINING

        public final ImageAspect aspect;

        public ImageViewDesc(Texture tex, TextureType type, Format format, int baseMip, int mipCount, int baseLayer, int layerCount, ImageAspect aspect) {
            this.tex = tex;
            this.type = type;
            this.format = format;
            this.baseMip = baseMip;
            this.baseLayer = baseLayer;
            this.mipCount = mipCount;
            this.layerCount = layerCount;
            this.aspect = aspect.resolve(format); // If the aspect is AUTO it generates based on the format
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ImageViewDesc that = (ImageViewDesc) o;
            return baseMip == that.baseMip && baseLayer == that.baseLayer && mipCount == that.mipCount && layerCount == that.layerCount && Objects.equals(tex, that.tex) && type == that.type && format == that.format && Objects.equals(aspect, that.aspect);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tex, type, format, baseMip, baseLayer, mipCount, layerCount, aspect);
        }
    }

    class ImageViewDescriptionBuilder {

        private final Texture tex;

        private TextureType type;
        private Format format;

        private int baseMip = 0, mipCount = -1;
        private int baseLayer = 0, layerCount = -1;

        private ImageAspect aspect = ImageAspect.AUTO;

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

        public ImageViewDescriptionBuilder aspect(ImageAspect aspect) {
            this.aspect = aspect;
            return this;
        }

        public ImageViewDesc build() {
            return new ImageViewDesc(tex, type, format, baseMip, mipCount, baseLayer, layerCount, aspect);
        }

    }

    default Texture parent() { return description().tex; }

    default TextureType type() { return description().type; }

    default int baseMipLevel() { return description().baseMip; }
    default int mipLevelCount() { return description().mipCount; }

    default int baseArrayLayer() { return description().baseLayer; }
    default int arrayLayerCount() { return description().layerCount; }

    default Format format() { return description().format; }

    ImageViewDesc description();

}
