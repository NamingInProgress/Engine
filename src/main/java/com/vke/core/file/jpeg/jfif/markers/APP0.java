package com.vke.core.file.jpeg.jfif.markers;

import com.vke.core.file.jpeg.jfif.JfifDataMarker;
import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class APP0 extends JfifDataMarker {
    public static final int[] JFIF_SIGNATURE = { 0x4A, 0x46, 0x49, 0x46, 0x00 };
    public static final int[] JFXX_SIGNATURE = { 0x4A, 0x46, 0x58, 0x58, 0x00 };

    public final int major, minor;
    public final int densityUnit;
    public final int xdensity, ydensity;

    public APP0(InputStream stream) throws IOException {
        super(stream);

        int[] sig = DataUtils.readU8N(5, stream);
        if (Arrays.equals(sig, JFXX_SIGNATURE)) {
            int toSkip = this.size - 2 - 5; //-5 for signature
            stream.skipNBytes(toSkip);

            throw new JFXXSkipException();
        } else if (!Arrays.equals(sig, JFIF_SIGNATURE)) {
            throw new IOException("Illegal JFIF/JFXX signature!");
        }

        this.major = DataUtils.readU8(stream);
        this.minor = DataUtils.readU8(stream);

        this.densityUnit = DataUtils.readU8(stream);
        this.xdensity = DataUtils.readU16BigEndian(stream);
        this.ydensity = DataUtils.readU16BigEndian(stream);

        int xthumb = DataUtils.readU8(stream);
        int ythumb = DataUtils.readU8(stream);
        int thumbnailByteSize = 3 * xthumb * ythumb;
        int[] _thumbnailData = DataUtils.readU8N(thumbnailByteSize, stream);
    }

    public static class JFXXSkipException extends IOException {}
}
