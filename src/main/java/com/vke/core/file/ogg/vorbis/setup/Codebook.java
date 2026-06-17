package com.vke.core.file.ogg.vorbis.setup;

import com.vke.core.file.deflate.decompress.huffman.Code;

public class Codebook {
    public final Code[] codewords;
    public final int dimensions, entries;
    public final float minVal, deltaVal;
    public final boolean seqP;
    public final long[] multiplicands;

    public Codebook(Code[] codewords, int dimensions, int entries) {
        this(codewords, dimensions, entries, 0, 0, false, null);
    }

    public Codebook(Code[] codewords, int dimensions, int entries, float minVal, float deltaVal, boolean seqP, long[] multiplicands) {
        this.codewords = codewords;
        this.dimensions = dimensions;
        this.entries = entries;
        this.minVal = minVal;
        this.deltaVal = deltaVal;
        this.seqP = seqP;
        this.multiplicands = multiplicands;
    }
}
