package com.vke.test.deFUCKINGflate;

import com.vke.core.file.deflate.exc.InflatingException;
import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DeflateTest1 {
    public static void main(String[] args) throws IOException, InflatingException {
        String str = "Hello world cool string abc abc abc abc abc lol lol 123 123 1234";
        byte[] input = str.getBytes(StandardCharsets.UTF_8);

        byte[] compressed = DataUtils.deflate(input);
        System.out.println("deflated");
        byte[] output = DataUtils.inflate(compressed);

        String str2 = new String(output, StandardCharsets.UTF_8);
        System.out.println(str2);
    }
}
