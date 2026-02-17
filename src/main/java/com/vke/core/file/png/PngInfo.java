package com.vke.core.file.png;

public class PngInfo {
    public int width, height;
    public boolean hasColorPalette;
    public PixelType pixelType;
    public int sampleBitDepth;
    public boolean hasAlphaChannel;
    public InterlacingMethod interlacingMethod;

    public int getSampleCount() {
        return pixelType.sampleCount + (hasAlphaChannel ? 1 : 0);
    }

    public int getPixelStride() {
        int bitsPerPixel = getSampleCount() * sampleBitDepth;
        return (bitsPerPixel + 7) / 8; // round up to full bytes
    }

    public enum PixelType {
        PaletteIndex(1),
        GrayScale(1),
        TrueColor(3);

        public final int sampleCount;

        PixelType(int sampleCount) {
            this.sampleCount = sampleCount;
        }
    }

    public enum InterlacingMethod {
        Sequential,
        Adam7
    }
}
