package com.vke;

import com.vke.annotations.Repeat;
import com.vke.annotations.Test;
import com.vke.annotations.lifecycle.BeforeAll;
import com.vke.core.file.deflate.BitUtils;
import com.vke.core.file.io.bit.BitOrdering;
import com.vke.core.file.io.bit.GoodBitInputStream;
import com.vke.core.file.io.bit.ShittyBitInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static com.vke.assertions.Assertions.*;

public class BitInputStreamTest {

    private static ShittyBitInputStream workingOne;
    private static GoodBitInputStream breakingOne;
    private static Random random = new Random();

    @BeforeAll
    public static void setup() {
        byte[] data = "Hello world 0103382 asjdasd".getBytes(StandardCharsets.UTF_8);
        InputStream isa = new ByteArrayInputStream(data.clone());
        InputStream isb = new ByteArrayInputStream(data);

        workingOne = new ShittyBitInputStream(isa);
        workingOne.setOrdering(BitOrdering.LSB_FIRST);

        breakingOne = new GoodBitInputStream(isb);
        breakingOne.setOrdering(BitOrdering.LSB_FIRST);
    }

    @Test
    @Repeat(1)
    public void compareBitInputStreams() throws IOException {
        boolean op = random.nextBoolean();
        op = false;
        int amt = random.nextInt(32);
        if (op) {
            int resA = workingOne.peekBits(amt);
            int resB = breakingOne.peekBits(amt);

            assertEquals(resA, resB, "Failed at peek(%d): expected %s, got %s".formatted(amt, BitUtils.intToBinStr(resA), BitUtils.intToBinStr(resB)));
        } else {
            int resA = workingOne.readBits(amt);
            int resB = breakingOne.readBits(amt);

            assertEquals(resA, resB, "Failed at read(%d): expected %s, got %s".formatted(amt, BitUtils.intToBinStr(resA), BitUtils.intToBinStr(resB)));
        }
    }

}
