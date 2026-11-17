package com.vke.core.file.exr.extract;

import com.vke.core.file.exr.ExrDecoder;
import com.vke.core.file.exr.head.ExrHeader;
import com.vke.core.file.exr.head.attr.Box2iAttr;
import com.vke.core.file.exr.out.ExrBitmap;
import com.vke.core.image.Bitmap;

import java.io.IOException;
import java.io.InputStream;

public class ExrScanlineBitmapExtractor extends ExrScanlineExtractor implements ExrBitmapExtractor {
    private final boolean flipVertically;
    private ExrBitmap bitmap;
    private int w, h;

    public ExrScanlineBitmapExtractor(ExrDecoder decoder, InputStream stream, boolean flipVertically) {
        super(decoder, stream);
        this.flipVertically = flipVertically;
    }

    private void createBitmap(ExrHeader header) {
        Box2iAttr dataWindow = header.dataWindow;
        Box2iAttr dispWindow = header.displayWindow;
        this.bitmap = new ExrBitmap(header.chlist, dataWindow.width(), dataWindow.height(), flipVertically);

        if (dataWindow.needsBg(dispWindow)) {
            //todo
            //bitmap.fill(RgbColor.VKE);
        }

        this.w = dataWindow.width();
        this.h = dataWindow.height();

        System.out.println("w: " + w + ",h: " + h);
    }

    @Override
    protected void onScanlinesRead(int part) throws IOException {
        ExrHeader header = decoder.parts[part].header();

        if (bitmap == null) {
            createBitmap(header);
        }

        int scanlineSize = w * header.computePixelStride();
        for (int scan = 0; scan < blockScans; scan++) {
            int scanY = yPos + scan;
            bitmap.setScanline(scanY, samples, scan * scanlineSize);
        }
    }

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }
}
