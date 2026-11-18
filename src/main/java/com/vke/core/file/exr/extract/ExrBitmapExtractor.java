package com.vke.core.file.exr.extract;

import com.vke.core.image.Bitmap;

public interface ExrBitmapExtractor extends ExrExtractor {
    Bitmap getBitmap();
}
