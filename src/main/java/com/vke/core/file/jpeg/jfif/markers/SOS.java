package com.vke.core.file.jpeg.jfif.markers;

import com.vke.core.file.jpeg.jfif.JfifDataMarker;
import com.vke.core.file.jpeg.jfif.ScanComponent;
import com.vke.core.file.utils.BitPackerUtils;
import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class SOS extends JfifDataMarker {
    public final int Ns;

    public final ScanComponent[] scanComponents;
    public final int Ss, Se;
    public final int Ah, Al;

    public SOS(InputStream stream, SOFn frameHeader) throws IOException {
        super(stream);

        if (frameHeader == null) throw new IOException("No SOFn Marker found yet! There must be a SOF (start of frame) header before any scans can start!");

        this.Ns = DataUtils.readU8(stream);

        this.scanComponents = new ScanComponent[Ns];
        for (int j = 0; j < Ns; j++) {
            int Csj = DataUtils.readU8(stream);
            int[] TdjTaj = BitPackerUtils.unpackU4BE(DataUtils.readU8(stream), 2);
            scanComponents[j] = new ScanComponent(Csj, TdjTaj[0], TdjTaj[1]);
        }

        this.Ss = DataUtils.readU8(stream);
        this.Se = DataUtils.readU8(stream);
        int[] AhAl = BitPackerUtils.unpackU4BE(DataUtils.readU8(stream), 2);
        this.Ah = AhAl[0];
        this.Al = AhAl[1];
    }
}
