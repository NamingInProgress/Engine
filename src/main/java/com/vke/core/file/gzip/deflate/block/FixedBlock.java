package com.vke.core.file.gzip.deflate.block;

import com.vke.core.file.gzip.deflate.DeflateBlock;
import com.vke.core.file.gzip.deflate.lz77.Lz77Decoder;
import com.vke.core.file.gzip.io.bit.BitInputStream;

import java.io.IOException;
import java.util.Arrays;

public class FixedBlock implements DeflateBlock {
    private static final int[] LITERAL_LENGTH_CODE_LENGTHS = new int[288];
    private static final int[] DISTANCE_CODE_LENGTHS = new int[32];

    private static final int LIT_LEN_CODE_LENGTH_0_143 = 8;
    private static final int LIT_LEN_CODE_LENGTH_144_255 = 9;
    private static final int LIT_LEN_CODE_LENGTH_256_279 = 7;
    private static final int LIT_LEN_CODE_LENGTH_280_287 = 8;
    private static final int DISTANCE_CODE_LENGTH = 5;

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

    public FixedBlock(boolean bFinal) {
        this.bFinal = bFinal;
        this.decoder = new Lz77Decoder(LITERAL_LENGTH_CODE_LENGTHS, DISTANCE_CODE_LENGTHS);
    }

    @Override
    public int nextByte(BitInputStream inputStream) throws IOException {
        return decoder.decodeNextByte(inputStream);
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
