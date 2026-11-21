package com.vke.core.file.exr.extract;

import com.vke.core.file.exr.ExrDecoder;
import com.vke.core.file.exr.head.ExrHeader;
import com.vke.core.file.exr.head.attr.Box2iAttr;
import com.vke.core.file.exr.out.ExrBitmap;
import com.vke.core.image.Bitmap;

import java.io.IOException;
import java.io.InputStream;

public class ExrTileBitmapExtractor extends ExrTileExtractor implements ExrBitmapExtractor {
    private final boolean flipVertically;
    private ExrBitmap bitmap;

    public ExrTileBitmapExtractor(ExrDecoder decoder, InputStream stream, boolean flipVertically) {
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
    }

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }

    @Override
    protected void onTileRead(int part) throws IOException {
        ExrHeader header = decoder.parts[part].header();
        if (bitmap == null) {
            createBitmap(header);
        }

        int scanlineSize = scanPixels * header.computePixelStride();
        for (int scan = 0; scan < tileScans; scan++) {
            int scanY = yPos + scan;
            bitmap.setShortScanline(xPos, scanY, samples, scan * scanlineSize, scanPixels);
        }
    }
}
