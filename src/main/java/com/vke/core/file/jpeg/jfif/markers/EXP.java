package com.vke.core.file.jpeg.jfif.markers;

import com.vke.core.file.jpeg.jfif.JfifDataMarker;
import com.vke.core.file.utils.BitPackerUtils;
import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class EXP extends JfifDataMarker {
    public final boolean expandV, expandH;

    public EXP(InputStream stream) throws IOException {
        super(stream);

        int[] EhEv = BitPackerUtils.unpackU4BE(DataUtils.readU8(stream), 2);
        this.expandH = EhEv[0] == 1;
        this.expandV = EhEv[1] == 1;
    }
}
