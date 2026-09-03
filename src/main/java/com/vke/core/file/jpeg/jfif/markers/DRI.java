package com.vke.core.file.jpeg.jfif.markers;

import com.vke.core.file.jpeg.jfif.JfifDataMarker;
import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class DRI extends JfifDataMarker {
    public final int Ri;

    public DRI(InputStream stream) throws IOException {
        super(stream);

        this.Ri = DataUtils.readU16BigEndian(stream);
    }
}
