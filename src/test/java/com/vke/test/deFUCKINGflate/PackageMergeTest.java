package com.vke.test.deFUCKINGflate;

import com.vke.core.file.deflate.compress.huffman.PackageMerge;

import java.util.Arrays;

public class PackageMergeTest {
    public static void main(String[] args) {
        int[] freq = {0, 0, 0, 1, 0, 0};
        int[] codeLengths = PackageMerge.perform(freq, 4);
        System.out.println(Arrays.toString(codeLengths));
    }
}
