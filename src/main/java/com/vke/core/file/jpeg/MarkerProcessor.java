package com.vke.core.file.jpeg;

import com.vke.core.file.jpeg.jfif.JfifDecoder;
import com.vke.core.file.jpeg.jfif.JfifMarker;
import com.vke.core.file.jpeg.jfif.Markers;
import com.vke.core.file.jpeg.jfif.markers.*;
import com.vke.core.file.jpeg.scan.Frame;
import com.vke.core.file.jpeg.scan.FrameKind;
import com.vke.core.file.jpeg.scan.Tables;
import com.vke.utils.Utils;

import java.io.IOException;
import java.io.InputStream;

public class MarkerProcessor {
    private final InputStream stream;
    private final JfifDecoder jfifDecoder;


    private Frame mainFrame;
    private Frame currentFrame;

    private boolean isHierachyMode;
    private EXP currentExp;

    private final Tables tables;

    public MarkerProcessor(InputStream stream) {
        this.stream = stream;
        this.jfifDecoder = new JfifDecoder(stream);
        this.tables = new Tables();
    }

    public void process() throws IOException {
        SOI soi = jfifDecoder.expectMarker(Markers.SOI);

        JfifMarker buffer = null;
        JfifMarker marker;
        //set marker to buffer and check if its null WHILE also resetting buffer and if still null, obtain next marker lol
        while (((marker = buffer) != null && (buffer = null) == Utils.NULL()) || (marker = jfifDecoder.nextMarker()) != null) {
            switch (marker) {
                case EOI _ -> {
                    break;
                }
                case SOFn sof -> {
                    currentFrame = sof.intoFrame();
                    if (!isHierachyMode) {
                        mainFrame = sof.intoFrame();
                    }

                    if (sof.kind == null) {
                        //dhp marker
                        isHierachyMode = true;
                    }
                }
                case SOS sos -> {
                    performScan(sos);
                }
                default -> {
                    buffer = handleMisc(marker);
                }
            }
        }
    }

    private JfifMarker handleMisc(JfifMarker marker) throws IOException {
        while (!Utils.TsContain(marker.getClass(), SOFn.class, SOS.class)) {
            switch (marker) {
                case DQT dqt -> {
                    for (DQT.Table table : dqt.tables) {
                        QuantTable qTable = new QuantTable(table.coefficients);
                        tables.installQuantTable(table.destination, qTable);
                    }
                }
                case DHT dht -> {
                    for (HuffmanTable huffmanTable : dht.huffmanTables) {
                        tables.installHuffmannTable(huffmanTable);
                    }
                }
                case DAC dac -> {
                    for (ArithmeticTable arithmeticTable : dac.arithmeticTables) {
                        tables.installArithmeticTable(arithmeticTable);
                    }
                }
                case EXP exp -> {
                    if (!isHierachyMode) {
                        throw new IOException("Cannot use EXP marker when not in hierachy mode.");
                    }
                    currentExp = exp;
                }

                default -> {
                    //ignore for now
                }
            }
            marker = this.jfifDecoder.nextMarker();
        }
        return marker;
    }

    private void performScan(SOS sos) throws IOException {
        JpegEcsByteInputStream jpegStream = new JpegEcsByteInputStream(stream);
        FrameKind kind = currentFrame.kind();
        if (kind == null) throw new IOException("Current SOF cant be a DHP marker!");

        if (kind.isLossless()) {

        }
    }
}
