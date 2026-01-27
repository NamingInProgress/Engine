package com.vke.test;

import com.vke.core.file.gzip.deflate.BitUtils;
import com.vke.core.file.gzip.io.bit.GoodBitInputStream;
import com.vke.core.file.gzip.io.bit.BitOrdering;
import com.vke.core.file.gzip.io.bit.ShittyBitInputStream;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class BitInputStreamTest {
    private static ShittyBitInputStream workingOne;
    private static GoodBitInputStream breakingOne;
    private static Random random = new Random();

    public static void main(String[] args) {
        byte[] data = "Hello world 0103382 asjdasd".getBytes(StandardCharsets.UTF_8);
        InputStream isa = new ByteArrayInputStream(data.clone());
        InputStream isb = new ByteArrayInputStream(data);

        workingOne = new ShittyBitInputStream(isa);
        workingOne.setOrdering(BitOrdering.LSB_FIRST);

        breakingOne = new GoodBitInputStream(isb);
        breakingOne.setOrdering(BitOrdering.LSB_FIRST);

        try {
            test();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void test() throws Exception {
        while (true) {
            testCall();
        }
    }

    private static void testCall() throws Exception {
        boolean op = random.nextBoolean();
        op = false;
        int amt = random.nextInt(32);
        if (op) {
            int resA = workingOne.peekBits(amt);
            int resB = breakingOne.peekBits(amt);
            if (resA != resB) {
                throw new Exception("failed at peek(" + amt + "): expected " + BitUtils.intToBinStr(resA) + ", got " + BitUtils.intToBinStr(resB));
            }
        } else {
            int resA = workingOne.readBits(amt);
            int resB = breakingOne.readBits(amt);
            if (resA != resB) {
                throw new Exception("failed at read(" + amt + "): expected " + BitUtils.intToBinStr(resA) + ", got " + BitUtils.intToBinStr(resB));
            }
        }
        System.out.println("call successfull");
    }
}
