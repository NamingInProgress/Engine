package com.vke.core.file.ogg.vorbis.data.window;

import com.vke.core.file.io.bit.BitStreamUtils;
import com.vke.core.file.io.bit.input.BitInputStream;
import com.vke.core.file.ogg.vorbis.data.Fn;
import com.vke.core.file.ogg.vorbis.header.IdentHeader;
import com.vke.core.file.ogg.vorbis.header.VorbisPCMInfoExtension;
import com.vke.core.file.ogg.vorbis.header.setup.Mode;

import java.io.IOException;

public class WindowDecoder {
    private final BitInputStream bitStream;
    private final VorbisPCMInfoExtension info;
    private final Mode mode;

    private boolean prevFlag;
    private boolean nextFlag;
    private int n;
    private int center;

    private long leftStart;
    private long leftEnd;
    private long leftN;

    private long rightStart;
    private long rightEnd;
    private long rightN;

    public WindowDecoder(VorbisPCMInfoExtension info, Mode mode, BitInputStream bitStream) throws IOException {
        this.info = info;
        this.mode = mode;
        this.bitStream = bitStream;

    }

    public WindowData decodeWindow() throws IOException {
        IdentHeader ident = info.identHeader();
        boolean blockFlag = mode.blockFlag();
        //so spec says window size can be at most 8192 or smth so int is fine
        if (mode.blockFlag()) {
            n = (int) ident.blockSize1;
            handleLongWindowShape();
        } else {
            n = (int) ident.blockSize0;
        }

        center = n / 2;

        if (blockFlag && !prevFlag) {
            long n4 = n / 4;
            long bs04 = ident.blockSize0 / 4;
            leftStart = n4 - bs04;
            leftEnd = n4 + bs04;
            leftN = ident.blockSize0 / 2;
        } else {
            leftStart = 0;
            leftEnd = center;
            leftN = center; // aka n/2
        }

        if (blockFlag && !nextFlag) {
            long n34 = n * 3L / 4;
            long bs04 = ident.blockSize0 / 4;
            rightStart = n34 - bs04;
            rightEnd = n34 + bs04;
            rightN = ident.blockSize0 / 2;
        } else {
            rightStart = center;
            rightEnd = n;
            rightN = center; //aka n/2
        }

        float[] window = new float[n];
        for (int i = (int) leftStart; i < leftEnd; i++) {
            window[i] = Fn.windowLeftSlope(i, leftStart, leftN);
        }
        for (int i = (int) leftEnd; i < rightStart; i++) {
            window[i] = 1.0f;
        }
        for (int i = (int) rightStart; i < rightEnd; i++) {
            window[i] = Fn.windowRightSlope(i, rightStart, rightN);
        }

        return new WindowData(window, n);
    }

    private void handleLongWindowShape() throws IOException {
        prevFlag = BitStreamUtils.readFlag(bitStream);
        nextFlag = BitStreamUtils.readFlag(bitStream);

    }
}
