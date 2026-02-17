package com.vke.core.file.png;

public class PngInfo {
    public boolean hasColorPalette;
    public PixelType pixelType;
    public int sampleBitDepth;
    public boolean hasAlphaChannel;
    public InterlacingMethod interlacingMethod;


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
