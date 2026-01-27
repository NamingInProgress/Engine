package com.vke.core.file.gzip.deflate.block;

import com.vke.core.file.gzip.deflate.DeflateBlock;
import com.vke.core.file.gzip.deflate.huffman.HMSymbolDecoder;
import com.vke.core.file.gzip.deflate.lz77.Lz77Decoder;
import com.vke.core.file.gzip.io.bit.BitInputStream;
import com.vke.core.file.gzip.io.bit.BitOrdering;

import java.io.IOException;
import java.util.Arrays;

public class DynamicBlock implements DeflateBlock {
    private static final int[] CODE_LENGTH_ORDER = {
            16,17,18,0,8,7,9,6,10,5,11,4,12,3,13,2,14,1,15
    };

    private final boolean bFinal;
    private boolean initialized;
    private Lz77Decoder decoder;
    private final int[] codeLenCodeLengths = new int[19];

    public DynamicBlock(boolean bFinal) {
        this.bFinal = bFinal;
    }

    @Override
    public int nextByte(BitInputStream in) throws IOException {
        if (!initialized) {
            initialize(in);
            initialized = true;
        }
        return decoder.decodeNextByte(in);
    }

    private void initialize(BitInputStream bitStream) throws IOException {
        bitStream.setOrdering(BitOrdering.LSB_FIRST);

        Arrays.fill(codeLenCodeLengths, 0);

        int HLIT  = bitStream.readBits(5) + 257;
        int HDIST = bitStream.readBits(5) + 1;
        int HCLEN = bitStream.readBits(4) + 4;

        for (int i = 0; i < HCLEN; i++) {
            codeLenCodeLengths[CODE_LENGTH_ORDER[i]] = bitStream.readBits(3);
        }

        HMSymbolDecoder codeLengthDecoder = new HMSymbolDecoder(codeLenCodeLengths);
        int total = HLIT + HDIST;
        int[] llAndDistLengths = new int[total];

        int index = 0;
        int prev = 0;

        while (index < total) {
            int sym = codeLengthDecoder.decodeSymbol(bitStream);

            bitStream.setOrdering(BitOrdering.LSB_FIRST);
            if (sym <= 15) {
                llAndDistLengths[index++] = sym;
                prev = sym;
            } else if (sym == 16) {
                int repeat = 3 + bitStream.readBits(2);
                for (int i = 0; i < repeat && index < total ; i++) {
                    llAndDistLengths[index++] = prev;
                }
            } else if (sym == 17) {
                int repeat = 3 + bitStream.readBits(3);
                for (int i = 0; i < repeat && index < total; i++) {
                    llAndDistLengths[index++] = 0;
                }
                prev = 0;
            } else if (sym == 18) {
                int repeat = 11 + bitStream.readBits(7);
                for (int i = 0; i < repeat && index < total; i++) {
                    llAndDistLengths[index++] = 0;
                }
                prev = 0;
            }
        }

        int[] literalLengthCodeLengths = Arrays.copyOfRange(llAndDistLengths, 0, HLIT);
        int[] distanceCodeLengths = Arrays.copyOfRange(llAndDistLengths, HLIT, HLIT + HDIST);

        boolean allZero = true;
        for (int len : distanceCodeLengths) {
            if (len != 0) {
                allZero = false;
                break;
            }
        }
        if (allZero) {
            distanceCodeLengths = new int[] { 1 };
        }

        this.decoder = new Lz77Decoder(literalLengthCodeLengths, distanceCodeLengths);
    }

    @Override
    public boolean isFinished() {
        return decoder.isFinished();
    }

    @Override
    public boolean bFinal() {
        return bFinal;
    }
}

