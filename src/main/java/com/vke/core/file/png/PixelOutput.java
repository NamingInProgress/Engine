package com.vke.core.file.png;

import java.awt.image.BufferedImage;

public class PixelOutput {
    private final PngInfo info;
    private final int[] pixels;

    public PixelOutput(PngInfo info) {
        this.info = info;
        this.pixels = new int[info.width * info.height];
    }

    void setPixelARGB(int i, int pixel) {
        pixels[i] = pixel;
    }

    public BufferedImage toJavaImage() {
        BufferedImage image = new BufferedImage(info.width, info.height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, info.width, info.height, pixels, 0, info.width);
        return image;
    }
}
