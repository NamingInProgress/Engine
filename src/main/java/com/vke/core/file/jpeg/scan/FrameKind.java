package com.vke.core.file.jpeg.scan;

import com.vke.api.rendering.abstraction.renderer.IntEnum;

public enum FrameKind implements IntEnum {
    BaselineDCT(0xC0, true, false, false, false),
    HuffExtSeqDCT(0xC1, true, false, false, false),
    HuffProgDCT(0xC2, true, true, false, false),
    HuffSeqLossless(0xC3, true, false, true, false),

    HuffDiffSeqDCT(0xC5, true, false, false, true),
    HuffDiffProgDCT(0xC6, true, true, false, true),
    HuffDiffLossless(0xC7, true, false, true, true),

    AriExtSeqDCT(0xC9, false, false, false, false),
    AriProgDCT(0xCA, false, true, false, false),
    AriSeqLossless(0xCB, false, false, true, false),

    AriDiffSeqDCT(0xCD, false, false, false, true),
    AriDiffProgDCT(0xCE, false, true, false, true),
    AriDiffLossless(0xCF, false, false, true, true);

    private final int marker;
    private final boolean huffman;
    private final boolean progressive;
    private final boolean lossless;
    private final boolean differential;

    FrameKind(int marker, boolean huffman, boolean progressive, boolean lossless, boolean differential) {
        this.marker = marker;
        this.huffman = huffman;
        this.progressive = progressive;
        this.lossless = lossless;
        this.differential = differential;
    }

    @Override
    public int getIntVal() {
        return marker;
    }

    public boolean usesHuffman() {
        return huffman;
    }

    public boolean usesArithmetic() {
        return !huffman;
    }

    public boolean isProgressive() {
        return progressive;
    }

    public boolean isLossless() {
        return lossless;
    }

    public boolean isDCT() {
        return !lossless;
    }

    public boolean isDifferential() {
        return differential;
    }
}
