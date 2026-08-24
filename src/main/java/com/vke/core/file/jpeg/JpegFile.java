package com.vke.core.file.jpeg;

import com.vke.core.file.jpeg.jfif.JfifDecoder;
import com.vke.core.file.jpeg.jfif.JfifMarker;
import com.vke.core.file.jpeg.jfif.Markers;
import com.vke.core.file.jpeg.jfif.markers.EOI;
import com.vke.core.file.jpeg.jfif.markers.SOI;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class JpegFile {


    public JpegFile(InputStream input) throws IOException {
        this(input, false);
    }

    public JpegFile(InputStream input, boolean flipY) throws IOException {
        BufferedInputStream stream = new BufferedInputStream(input);
        MarkerProcessor processor = new MarkerProcessor(stream);
        processor.process();
    }
}
