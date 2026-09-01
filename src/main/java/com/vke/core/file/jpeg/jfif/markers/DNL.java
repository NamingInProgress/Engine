package com.vke.core.file.jpeg.jfif.markers;

import com.vke.core.file.jpeg.jfif.JfifDataMarker;
import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class DNL extends JfifDataMarker {
    public final int NL;

    public DNL(InputStream stream) throws IOException {
        super(stream);

        NL = DataUtils.readU16BigEndian(stream);
    }
}
