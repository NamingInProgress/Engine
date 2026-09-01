package com.vke.core.file.jpeg.jfif.markers;

import com.vke.core.file.jpeg.jfif.JfifDataMarker;
import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class APP1 extends JfifDataMarker {
    public APP1(InputStream stream) throws IOException {
        super(stream);

        System.err.println("JPEG DECODER: Exif standard is currently not supported. Please use only JFIF for now. Skipping APP1 segment...");

        int toSkip = this.size - 2;
        DataUtils.readU8N(toSkip, stream);
    }
}
