package com.vke.core.file.ogg.vorbis.header.setup;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.vke.core.file.deflate.decompress.huffman.Code;
import com.vke.core.file.deflate.decompress.huffman.HMSymbolDecoder;
import com.vke.core.file.io.bit.BitOrdering;
import com.vke.core.file.io.bit.input.BitInputStream;

import java.io.IOException;

public class Codebook {
    public final Code[] codewords;
    public final int dimensions, entries;
    public final float minVal, deltaVal;
    public final boolean seqP;
    public final long[] multiplicands;

    private HMSymbolDecoder decoder;
    private final int lookupType;
    private final int lookupValues;

    private IntObjectHashMap<float[]> vqCache;

    public Codebook(Code[] codewords, int dimensions, int entries, int lookupType) {
        this(codewords, dimensions, entries, 0, 0, false, null, lookupType, 0);
    }

    public Codebook(Code[] codewords, int dimensions, int entries, float minVal, float deltaVal, boolean seqP, long[] multiplicands, int lookupType, int lookupValues) {
        this.codewords = codewords;
        this.dimensions = dimensions;
        this.entries = entries;
        this.minVal = minVal;
        this.deltaVal = deltaVal;
        this.seqP = seqP;
        this.multiplicands = multiplicands;
        this.lookupType = lookupType;
        this.lookupValues = lookupValues;

        this.vqCache = new IntObjectHashMap<>();
    }

    public HMSymbolDecoder getSymbolDecoder() {
        if (decoder == null) {
            decoder = new HMSymbolDecoder(codewords, BitOrdering.LSB_FIRST);
        }

        return decoder;
    }

    public float[] decodeVQ(BitInputStream stream) throws IOException {
        return performVQLookup(getSymbolDecoder().decodeSymbol(stream));
    }

    public float[] performVQLookup(int entry) {
        float[] maybeCached = vqCache.get(entry);
        if (maybeCached != null) {
            return maybeCached.clone();
        }

        float[] vector = new float[dimensions];
        if (lookupType == 0) {
            return new float[dimensions];
        } else if (lookupType == 1) {
            float last = 0;
            int lookupDivisor = 1;
            for (int i = 0; i < dimensions; i++) {
                int off = (entry / lookupDivisor) % lookupValues;
                vector[i] = multiplicands[off] * deltaVal + minVal + last;
                if (seqP) {
                    last = vector[i];
                }
                lookupDivisor *= lookupValues;
            }

            //appearently only caching here is better since type 2 doesnt get resued at all
            vqCache.put(entry, vector);
        } else if (lookupType == 2) {
            float last = 0;
            int off = entry * dimensions;
            for (int i = 0; i < dimensions; i++) {
                vector[i] = multiplicands[off] * deltaVal + minVal + last;
                if (seqP) {
                    last = vector[i];
                }
                off++;
            }
        }
        return vector;
    }
}
