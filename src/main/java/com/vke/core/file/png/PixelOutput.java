package com.vke.core.file.png;

import java.awt.image.BufferedImage;

public class PixelOutput {
    private final PngInfo info;
    private final int[] pixels;

    public PixelOutput(PngInfo info) {
        this.info = info;
        this.pixels = new int[info.width * info.height];
    }

    public void readSamples(byte[] samples, int[][] palette) {
        int cursor = 0;
        int size = info.width * info.height;

        switch (info.pixelType) {
            case PaletteIndex -> {
                for (int i = 0; i < size; i++) {
                    int index = samples[i];
                    pixels[i] = rgba(palette[index][0], palette[index][1], palette[index][2], 255);
                }
            }
            case GrayScale -> {
                for (int i = 0; i < size; i++) {
                    int gray = samples[cursor++] & 0xFF;
                    int alpha = info.hasAlphaChannel ? samples[cursor++] & 0xFF : 255;
                    pixels[i] = rgba(gray, gray, gray, alpha);
                }
            }
            case TrueColor -> {
                for (int i = 0; i < size; i++) {
                    int r = samples[cursor++] & 0xFF;
                    int g = samples[cursor++] & 0xFF;
                    int b = samples[cursor++] & 0xFF;
                    int a = info.hasAlphaChannel ? samples[cursor++] & 0xFF : 255;
                    pixels[i] = rgba(r, g, b, a);
                }
            }
        }
    }

    private int rgba(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public BufferedImage toJavaImage() {
        BufferedImage image = new BufferedImage(info.width, info.height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, info.width, info.height, pixels, 0, info.width);
        return image;
    }
}
