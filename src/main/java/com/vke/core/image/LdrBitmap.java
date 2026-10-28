package com.vke.core.image;

import com.vke.api.rendering.abstraction.renderer.enums.texture.Format;

import java.nio.ByteBuffer;

public class LdrBitmap implements Bitmap {
    private static final int CHANNELS = 4;

    private final int width, height;
    private final boolean flipVertically;
    private final byte[] rgbaBytes;

    public LdrBitmap(int width, int height, boolean flipVertically) {
        this.width = width;
        this.height = height;
        this.flipVertically = flipVertically;
        this.rgbaBytes = new byte[width * height * CHANNELS];
    }

    public void setPixelRGBA(int x, int y, int rgba) {
        setPixelRGBA_0(getIndex(x, y), rgba);
    }

    public void setPixelRGBA(int i, int rgba) {
        setPixelRGBA_0(processIndex(i), rgba);
    }

    private void setPixelRGBA_0(int i, int rgba) {
        rgbaBytes[i] = (byte) (rgba >>> 24);
        rgbaBytes[i + 1] = (byte) (rgba >>> 16);
        rgbaBytes[i + 2] = (byte) (rgba >>> 8);
        rgbaBytes[i + 3] = (byte) rgba;
    }

    public void setPixelARGB(int x, int y, int argb) {
        setPixelARGB_0(getIndex(x, y), argb);

    }

    public void setPixelARGB(int i, int argb) {
        setPixelARGB_0(processIndex(i), argb);
    }

    private void setPixelARGB_0(int i, int argb) {
        rgbaBytes[i] = (byte) (argb >>> 16);
        rgbaBytes[i + 1] = (byte) (argb >>> 8);
        rgbaBytes[i + 2] = (byte) argb;
        rgbaBytes[i + 3] = (byte) (argb >>> 24);
    }

    private int getIndex(int x, int y) {
        if (flipVertically) y = height - 1 - y;
        return (x + y * width) * CHANNELS;
    }

    private int processIndex(int i) {
        if (flipVertically) {
            int x = i % width;
            int y = i / width;

            i = (height - 1 - y) * width + x;
        }
        return i * CHANNELS;
    }

    @Override
    public Format dataFormat() {
        return Format.RGBA8;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public boolean hasAlphaChannel() {
        return true;
    }

    @Override
    public int pixelStride() {
        return CHANNELS;
    }

    @Override
    public void uploadIntoBytebuffer(ByteBuffer buffer) {
        buffer.put(rgbaBytes);
    }
}
