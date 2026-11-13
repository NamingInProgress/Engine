package com.vke.core.file.deflate.decompress.block;

import com.vke.core.file.deflate.decompress.DeflateBlock;
import com.vke.core.file.deflate.decompress.check.Checksum32;
import com.vke.core.file.deflate.decompress.lz77.Lz77Decoder;
import com.vke.core.file.deflate.decompress.lz77.SlidingWindow;
import com.vke.core.file.io.bit.input.BitInputStream;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;

public class FixedBlock implements DeflateBlock {
    public static final int[] LITERAL_LENGTH_CODE_LENGTHS = new int[288];
    public static final int[] DISTANCE_CODE_LENGTHS = new int[32];

    public static final int LIT_LEN_CODE_LENGTH_0_143 = 8;
    public static final int LIT_LEN_CODE_LENGTH_144_255 = 9;
    public static final int LIT_LEN_CODE_LENGTH_256_279 = 7;
    public static final int LIT_LEN_CODE_LENGTH_280_287 = 8;
    public static final int DISTANCE_CODE_LENGTH = 5;

    static {
        int i = 0;
        for (;i <= 143; i++) {
            LITERAL_LENGTH_CODE_LENGTHS[i] = LIT_LEN_CODE_LENGTH_0_143;
        }
        for (;i <= 255; i++) {
            LITERAL_LENGTH_CODE_LENGTHS[i] = LIT_LEN_CODE_LENGTH_144_255;
        }
        for (;i <= 279; i++) {
            LITERAL_LENGTH_CODE_LENGTHS[i] = LIT_LEN_CODE_LENGTH_256_279;
        }
        for (;i <= 287; i++) {
            LITERAL_LENGTH_CODE_LENGTHS[i] = LIT_LEN_CODE_LENGTH_280_287;
        }

        Arrays.fill(DISTANCE_CODE_LENGTHS, DISTANCE_CODE_LENGTH);
        DISTANCE_CODE_LENGTHS[30] = 0;
        DISTANCE_CODE_LENGTHS[31] = 0;
    }

    private final boolean bFinal;
    private final Lz77Decoder decoder;

    public FixedBlock(boolean bFinal, SlidingWindow window) {
        this.bFinal = bFinal;
        this.decoder = new Lz77Decoder(LITERAL_LENGTH_CODE_LENGTHS, DISTANCE_CODE_LENGTHS, window);
    }

    @Override
    public int inflateBytes(BitInputStream inputStream, byte[] dst, int start, int length, @Nullable Checksum32 checksum) throws IOException {
        //todo: make lz77 decoder have a method for bulk decoding, this can be used in the active copy path for example.
        //for huffman, i have no clue yet on how to bulk decode, but maybe its not even necessary. important thing for now is that
        //the lz copy thing is optimized and that the huffman loop is inside the decoder so we have so many less interface calls.
        //i hope that these changes will speed up the process significantly
        if (decoder.isFinished()) return -1;
        for (int i = 0; i < length; i++) {
            if (decoder.isFinished()) {
                return i;
            }
            int b = decoder.decodeNextByte(inputStream);
            if (b == -1) {
                return i;
            }
            if (checksum != null) checksum.nextByte(b);
            dst[start + i] = (byte) b;
        }
        return length;
    }

    @Override
    public boolean bFinal() {
        return bFinal;
    }
}
