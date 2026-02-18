package com.vke.core.file.png.adam7;

import com.vke.core.file.png.PixelOutput;
import com.vke.core.file.png.PngInfo;
import com.vke.core.file.png.SequentialImageSampler;
import com.vke.core.file.png.chunks.scanlines.Scanline;
import com.vke.core.file.zlib.ZlibDecompressor;

import java.io.IOException;

public class Adam7ImageSampler {
    private static final int[][] ADAM7 = {
            {0, 0, 8, 8},
            {4, 0, 8, 8},
            {0, 4, 4, 8},
            {2, 0, 4, 4},
            {0, 2, 2, 4},
            {1, 0, 2, 2},
            {0, 1, 1, 2}
    };

    public static PixelOutput sample(PngInfo info, ZlibDecompressor decompressor, int[][] palette) throws IOException {
        decompressor.parseHeader();

        byte[] samples = new byte[info.width * info.height * info.getPixelStride()];

        for (int pass = 0; pass < 7; pass++) {
            int startX = ADAM7[pass][0];
            int startY = ADAM7[pass][1];
            int stepX  = ADAM7[pass][2];
            int stepY  = ADAM7[pass][3];

            int passWidth  = (info.width  - startX + stepX - 1) / stepX;
            int passHeight = (info.height - startY + stepY - 1) / stepY;

            if (passWidth <= 0 || passHeight <= 0)
                continue;

            Scanline previous = null;

            for (int y = 0; y < passHeight; y++) {
                Scanline scanline = new Scanline(previous, info, decompressor, passWidth);

                byte[] unfiltered = scanline.unfilteredBytes;

                for (int x = 0; x < passWidth; x++) {
                    int globalX = startX + x * stepX;
                    int globalY = startY + y * stepY;

                    int pixelStride = info.getPixelStride();

                    int destIndex = (globalY * info.width + globalX) * pixelStride;

                    int srcIndex = x * pixelStride;

                    System.arraycopy(
                            unfiltered,
                            srcIndex,
                            samples,
                            destIndex,
                            pixelStride
                    );
                }

                previous = scanline;
            }
        }

        PixelOutput output = new PixelOutput(info);
        SequentialImageSampler.readSamples(info, output, samples, palette);
        return output;
    }
}
