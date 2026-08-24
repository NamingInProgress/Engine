package com.vke.core.file.jpeg.jfif;

import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public abstract class JfifDataMarker extends JfifMarker {
    protected final int size;

    public JfifDataMarker(InputStream stream) throws IOException {
        this.size = DataUtils.readU16BigEndian(stream);
    }
}
