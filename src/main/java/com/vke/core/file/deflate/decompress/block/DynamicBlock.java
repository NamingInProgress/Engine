package com.vke.core.file.deflate.decompress.block;

import com.vke.core.file.deflate.decompress.BitUtils;
import com.vke.core.file.deflate.decompress.DeflateBlock;
import com.vke.core.file.deflate.decompress.huffman.HMSymbolDecoder;
import com.vke.core.file.deflate.decompress.lz77.Lz77Decoder;
import com.vke.core.file.deflate.decompress.lz77.SlidingWindow;
import com.vke.core.file.io.bit.input.BitInputStream;
import com.vke.core.file.io.bit.BitOrdering;
import com.vke.core.file.io.bit.input.ShittyBitInputStream;

import java.io.IOException;
import java.util.Arrays;

public class DynamicBlock implements DeflateBlock {
    public static final int[] CODE_LENGTH_ORDER = {
            16,17,18,0,8,7,9,6,10,5,11,4,12,3,13,2,14,1,15
    };

    private final boolean bFinal;
    private boolean initialized;
    private Lz77Decoder decoder;
    private final int[] codeLenCodeLengths = new int[19];
    private int windowSize;
    private SlidingWindow windowReference;

    public DynamicBlock(boolean bFinal, SlidingWindow window) {
        this.bFinal = bFinal;
        this.windowReference = window;
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

        //System.out.println("HLIT = " + HLIT);
        //System.out.println("HDIST = " + HDIST);
        //System.out.println("HCLEN = " + HCLEN);

        for (int i = 0; i < HCLEN; i++) {
            int bits = bitStream.readBits(3);
            codeLenCodeLengths[CODE_LENGTH_ORDER[i]] = bits;
        }

        //System.out.println("CODE LENGTH CODE LENGTHS:");
        //System.out.println(Arrays.toString(codeLenCodeLengths));

        HMSymbolDecoder codeLengthDecoder = new HMSymbolDecoder(codeLenCodeLengths, BitOrdering.LSB_FIRST);
        int total = HLIT + HDIST;
        int[] llAndDistLengths = new int[total];

        int index = 0;
        int prev = 0;

        //System.out.println("MAX SYMBOLS ALLOWED: " + total);
        while (index < total) {
            int sym = codeLengthDecoder.decodeSymbol(bitStream);

            bitStream.setOrdering(BitOrdering.LSB_FIRST);
            if (sym <= 15) {
                llAndDistLengths[index] = sym;
                //System.out.println("[DEC] literal length: " + sym + " -> position " + index);

                index++;
                prev = sym;

            } else if (sym == 16) {
                int extra = bitStream.readBits(2);
                int repeat = 3 + extra;

                //System.out.println("[DEC] repeat prev=" + prev +
                //        " extra=" + BitUtils.intToBinStr(extra) +
                //        " repeat=" + repeat +
                //        " starting at index=" + index);

                for (int i = 0; i < repeat && index < total; i++) {
                    llAndDistLengths[index++] = prev;
                }

            } else if (sym == 17) {
                int extra = bitStream.readBits(3);
                int repeat = 3 + extra;

                //System.out.println("[DEC] repeat ZERO(17) extra=" + extra +
                //        " repeat=" + repeat +
                //        " starting at index=" + index);

                for (int i = 0; i < repeat && index < total; i++) {
                    llAndDistLengths[index++] = 0;
                }

                prev = 0;

            } else if (sym == 18) {
                int extra = bitStream.readBits(7);
                int repeat = 11 + extra;

                //System.out.println("[DEC] repeat ZERO(18) extra=" + extra +
                //        " repeat=" + repeat +
                //        " starting at index=" + index);

                for (int i = 0; i < repeat && index < total; i++) {
                    llAndDistLengths[index++] = 0;
                }

                prev = 0;
            }
        }

        //System.out.println("INDEX: " + index);
        //System.out.println("ALL: ");
        //System.out.println(Arrays.toString(llAndDistLengths));

        int[] literalLengthCodeLengths = Arrays.copyOfRange(llAndDistLengths, 0, HLIT);
        int[] distanceCodeLengths = Arrays.copyOfRange(llAndDistLengths, HLIT, HLIT + HDIST);

        //System.out.println("LIT LEN CODES:");
        //System.out.println(Arrays.toString(literalLengthCodeLengths));

        boolean allZero = true;
        for (int len : distanceCodeLengths) {
            if (len != 0) {
                allZero = false;
                break;
            }
        }
        allZero = HDIST == 1;
        if (allZero) {
            distanceCodeLengths = new int[] { 1 };
        }

        //System.out.println("DIST CODES:");
        //System.out.println(Arrays.toString(distanceCodeLengths));

        //System.out.println("===== HEADER DONE =====");

        this.decoder = new Lz77Decoder(literalLengthCodeLengths, distanceCodeLengths, windowReference);
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

