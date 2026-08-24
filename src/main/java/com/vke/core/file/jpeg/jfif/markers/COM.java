package com.vke.core.file.jpeg.jfif.markers;

import com.vke.core.file.jpeg.jfif.JfifDataMarker;

import java.io.IOException;
import java.io.InputStream;

public class COM extends JfifDataMarker {
    public COM(InputStream stream) throws IOException {
        super(stream);

        int toSkip = this.size - 2;
        stream.skipNBytes(toSkip);
    }
}
