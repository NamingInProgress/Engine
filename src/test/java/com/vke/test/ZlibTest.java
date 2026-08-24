package com.vke.test;

import com.carrotsearch.hppc.ByteArrayList;
import com.vke.core.FileIdentifier;
import com.vke.core.file.io.bit.input.ShittyBitInputStream;
import com.vke.core.file.zlib.ZlibDecompressor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;

public class ZlibTest {
    public static void main(String[] args) throws IOException {
        InputStream is = FileIdentifier.of("test.txt").openInputStream();
        String data = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        byte[] raw = data.getBytes(StandardCharsets.UTF_8);

        Deflater deflater = new Deflater(
                Deflater.DEFAULT_COMPRESSION,
                false // IMPORTANT: false = zlib format (header + checksum)
        );

        InputStream rawData = new DeflaterInputStream(
                new ByteArrayInputStream(raw),
                deflater
        );

        ZlibDecompressor decompressor = new ZlibDecompressor(new ShittyBitInputStream(rawData));
        decompressor.parseHeader();
        ByteArrayList bal = new ByteArrayList();
        while(true) {
            int next = decompressor.nextByte();
            if (next == -1) break;
            bal.add((byte) next);
        }
        //decompressor.parseFooter();

        String read = new String(bal.toArray(), StandardCharsets.UTF_8);
        System.out.println(read);
    }
}
