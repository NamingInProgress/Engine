package com.vke.test.deFUCKINGflate;

import com.carrotsearch.hppc.ByteArrayList;
import com.vke.core.file.deflate.compress.DeflatingDevice;
import com.vke.core.file.deflate.decompress.InflatingDevice;
import com.vke.core.file.deflate.exc.InflatingException;
import com.vke.utils.Identifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;

public class FixedBlockTest {
    public static void main(String[] args) throws IOException, InflatingException, InterruptedException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();

        InputStream testStream = new Identifier("test.txt").asInputStream();
        byte[] testData = testStream.readAllBytes();
        //byte[] testData = "abc abc abc".getBytes(StandardCharsets.UTF_8);
        DeflatingDevice device = new DeflatingDevice(bao, 64);
        device.deflateNext(testData);
        device.finish();

        byte[] output = bao.toByteArray();

        float lenIn = testData.length;
        float lenOut = output.length;
        System.out.println("comrpession ratio: " + (lenIn / lenOut));

        System.out.println("------------------------------------------------------");

        ByteArrayInputStream bai = new ByteArrayInputStream(output);
        InflatingDevice inflater = new InflatingDevice(null, bai);

        ByteArrayList list = new ByteArrayList();
        while (!inflater.isFinished()) {
            int r = inflater.inflateNextByte();
            if (r == -1) break;
            list.add((byte) r);
        }
        byte[] inflated = list.toArray();
        String inflatedStr = new String(inflated, StandardCharsets.UTF_8);
        System.out.println(inflatedStr);
    }
}
