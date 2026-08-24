package com.vke.core.file.jpeg.jfif;

import com.vke.core.file.jpeg.jfif.markers.*;
import com.vke.core.file.utils.DataUtils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class JfifDecoder {
    private final InputStream input;

    private SOFn lastFrameHeader;
    private int lastMarker;

    public JfifDecoder(InputStream input) {
        this.input = input;
    }

    @SuppressWarnings("unchecked")
    public <T extends JfifMarker> T expectMarker(int marker) throws IOException {
        JfifMarker m = nextMarker();
        if (lastMarker != marker) {
            throw new IOException("Illegal Marker found -> needs " + Integer.toHexString(marker));
        }
        return (T) m;
    }

    @SuppressWarnings("all")
    public JfifMarker nextMarker() throws IOException {
        DataUtils.enableThrowOnEOF();
        int marker;
        try {
            marker = DataUtils.readU8(input);
            while (marker != 0xFF) {
                marker = DataUtils.readU8(input);
            }
            while (marker == 0xFF) {
                marker = DataUtils.readU8(input);
            }
        } catch (EOFException ignore) {
            return null;
        }
        if (marker == 0x00) {
            //skip stuffing bytes
            DataUtils.popConfig();
            return nextMarker();

            //0xFF 0x00
        }
        try {
            JfifMarker m = switch (marker) {
                case Markers.SOI -> new SOI();
                case Markers.EOI -> new EOI();
                case Markers.SOS -> new SOS(input, lastFrameHeader);
                case Markers.APP0 -> new APP0(input);
                case Markers.APP1 -> new APP1(input);
                case Markers.DQT -> new DQT(input);
                case Markers.DHT -> new DHT(input);
                case Markers.SOF0 -> new SOFn(input, marker);
                case Markers.SOF1 -> new SOFn(input, marker);
                case Markers.SOF2 -> new SOFn(input, marker);
                case Markers.SOF3 -> new SOFn(input, marker);
                case Markers.SOF9 -> new SOFn(input, marker);
                case Markers.SOF10 -> new SOFn(input, marker);
                case Markers.SOF11 -> new SOFn(input, marker);
                case Markers.DAC -> new DAC(input);
                case Markers.DRI -> new DRI(input);
                case Markers.COM -> new COM(input);
                case Markers.DNL -> new DNL(input);
                case Markers.EXP -> new EXP(input);
                case Markers.DHP -> new SOFn(input, marker);
                default -> {
                    /*skip until next marker*/
                    yield nextMarker();
                }
            };

            lastMarker = marker;

            if (m instanceof SOFn sofn) {
                lastFrameHeader = sofn;
            }

            DataUtils.popConfig();
            return m;
        } catch (APP0.JFXXSkipException _) {
            return nextMarker();
        }
    }
}
