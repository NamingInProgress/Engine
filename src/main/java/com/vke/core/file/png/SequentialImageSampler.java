package com.vke.core.file.png;

import com.vke.core.file.png.chunks.scanlines.Scanline;
import com.vke.core.file.zlib.ZlibDecompressor;

import java.io.IOException;

public class SequentialImageSampler {
    public static Pixels sample(PngInfo info, ZlibDecompressor idat, int[][] palette) throws IOException {
        byte[] samples = unfilterIdatDataSequential(info, idat);
        Pixels output = new Pixels(info);
        readSamples(info, output, samples, palette);
        return output;
    }

    private static byte[] unfilterIdatDataSequential(PngInfo pngInfo, ZlibDecompressor decompressor) throws IOException {
        decompressor.parseHeader();
        int bytesPerPixel = pngInfo.getPixelStride();
        int scanlineLength = bytesPerPixel * pngInfo.width;

        byte[] imageData = new byte[scanlineLength * pngInfo.height];

        Scanline prev = null;
        Scanline curr = null;
        int offset = 0;

        for (int y = 0; y < pngInfo.height; y++) {
            curr = new Scanline(prev, pngInfo, decompressor);

            System.arraycopy(curr.unfilteredBytes, 0, imageData, offset, scanlineLength);
            offset += scanlineLength;

            prev = curr;
        }

        //decompressor.parseFooter();

        return imageData;
    }

    public static void readSamples(PngInfo info, Pixels output, byte[] samples, int[][] palette) {
        int cursor = 0;
        int size = info.width * info.height;

        switch (info.pixelType) {
            case PaletteIndex -> {
                for (int i = 0; i < size; i++) {
                    int index = samples[i];
                    int pixel = rgba(palette[index][0], palette[index][1], palette[index][2], 255);
                    output.setPixelARGB(i, pixel);
                }
            }
            case GrayScale -> {
                for (int i = 0; i < size; i++) {
                    int gray = samples[cursor++] & 0xFF;
                    int alpha = info.hasAlphaChannel ? samples[cursor++] & 0xFF : 255;
                    int pixel = rgba(gray, gray, gray, alpha);
                    output.setPixelARGB(i, pixel);
                }
            }
            case TrueColor -> {
                for (int i = 0; i < size; i++) {
                    int r = samples[cursor++] & 0xFF;
                    int g = samples[cursor++] & 0xFF;
                    int b = samples[cursor++] & 0xFF;
                    int a = info.hasAlphaChannel ? samples[cursor++] & 0xFF : 255;
                    int pixel = rgba(r, g, b, a);
                    output.setPixelARGB(i, pixel);
                }
            }
        }
    }

    private static int rgba(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
