package com.vke.core.file.ogg.vorbis.header.setup.huffman;

import com.vke.core.file.deflate.decompress.huffman.Code;
import com.vke.core.file.ogg.vorbis.header.setup.Codeword;

public class VorbisHMCodeGenerator {
    public static Code[] generateVorbisCodes(Codeword[] codewords) {
        Code[] codes = new Code[codewords.length];

        int[] nextCode = new int[33];

        for (int i = 0; i < codewords.length; i++) {
            int len = codewords[i].length();
            if (codewords[i].unused()) {
                codes[i] = new Code(0, 0, i);
                continue;
            }

            int rawCode = nextCode[len];
            int reversedCode = Integer.reverse(rawCode) >>> (32 - len);

            codes[i] = new Code(reversedCode, len, i);

            int entry = len;
            while (entry > 0) {
                nextCode[entry]++;
                if ((nextCode[entry] & 1) == 0) {
                    entry--;
                } else {
                    break;
                }
            }

            if (entry > 0) {
                for (int bit = len + 1; bit <= 32; bit++) {
                    nextCode[bit] = nextCode[bit - 1] << 1;
                }
            }
        }
        return codes;
    }
}
