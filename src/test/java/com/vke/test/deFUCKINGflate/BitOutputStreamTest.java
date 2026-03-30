package com.vke.test.deFUCKINGflate;

import com.vke.core.file.deflate.decompress.BitUtils;
import com.vke.core.file.io.bit.output.BitOutputStream;
import com.vke.core.file.io.bit.output.GoodBitOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BitOutputStreamTest {
    public static void main(String[] args) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        BitOutputStream bos = new GoodBitOutputStream(outputStream);
        bos.setPaddingBit(0);

        bos.writeBits(3, 2);
        bos.writeBits(0, 6);
        bos.writeBits(7, 3);
        bos.writeBits(0, 2);
        bos.writeBits((1 << 5) - 1, 5); //11111

        bos.flushBuffer();
        outputStream.flush();

        byte[] result = outputStream.toByteArray();
        for (byte b : result) {
            System.out.println(BitUtils.intToBinStr(b));
        }
    }
}
