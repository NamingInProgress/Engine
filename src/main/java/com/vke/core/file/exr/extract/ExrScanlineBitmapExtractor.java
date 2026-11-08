package com.vke.core.file.exr.extract;

import com.vke.core.color.RgbColor;
import com.vke.core.file.exr.ExrDecoder;
import com.vke.core.file.exr.head.ExrHeader;
import com.vke.core.file.exr.head.attr.Box2iAttr;
import com.vke.core.file.utils.DataUtils;
import com.vke.core.image.Bitmap;
import com.vke.core.image.LdrBitmap;

import java.io.IOException;
import java.io.InputStream;

public class ExrScanlineBitmapExtractor extends ExrScanlineExtractor {
    private final int targetPart;
    private final ExrHeader header;

    public ExrScanlineBitmapExtractor(ExrDecoder decoder, InputStream stream, int targetPart) {
        super(decoder, stream);
        this.targetPart = targetPart;
        this.header = decoder.parts[targetPart].header();
    }

    public Bitmap readIntoBitmap(boolean flipVertically) throws IOException {
        Box2iAttr dataWindow = header.dataWindow;
        Box2iAttr dispWindow = header.displayWindow;
        LdrBitmap bitmap = new LdrBitmap(dispWindow.width(), dispWindow.height(), flipVertically);
        if (dataWindow.needsBg(dispWindow)) {
            System.out.println("filling bg");
            bitmap.fill(RgbColor.VKE);
        }

        int w = dataWindow.width();
        int h = dataWindow.height();

        System.out.println("w: " + w + ",h: " + h);

        more = true;
        while (more) {
            readScanlines();
            if (part != targetPart) {
                continue;
            }

            for (int scan = 0; scan < blockScans; scan++) {
                int scanY = yPos + scan;
                for (int x = 0; x < w; x++) {
                    int r = samples[x];
                    //int g = samples[x * 4 + 1];
                    //int b = samples[x * 4 + 2];
                    //int a = samples[x * 4 + 3];
                    //int rgba = DataUtils.abcd(r, g, b, a);
                    int rgba = DataUtils.abcd(r, r, r, 255);
                    //todo: replace LdrBitmap with a custom ExrBitmap and then make it vulkan compatible also
                    bitmap.setPixelRGBA(x, scanY, rgba);
                }
            }
        }

        return bitmap;
    }
}
