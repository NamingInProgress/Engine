package com.vke.core.file.png;

import com.vke.core.memory.AutoHeapAllocator;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Pixels {
    private final PngInfo info;
    private final int[] pixels;

    public Pixels(PngInfo info) {
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

    public ByteBuffer argbToByteBuffer(AutoHeapAllocator alloc) {
        ByteBuffer buffer = alloc.allocByteBuffer(pixels.length * 4).getHeapObject();
        buffer.order(ByteOrder.BIG_ENDIAN);

        for (int pixel : pixels) {
            buffer.put((byte) ((pixel >> 16) & 0xFF)); // R
            buffer.put((byte) ((pixel >>  8) & 0xFF)); // G
            buffer.put((byte) ( pixel        & 0xFF)); // B
            buffer.put((byte) ((pixel >> 24) & 0xFF)); // A
        }

        buffer.flip();
        return buffer;
    }
}
