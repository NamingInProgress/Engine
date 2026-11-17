package com.vke.core.file.deflate.decompress.huffman;

import com.vke.core.file.deflate.decompress.BitUtils;
import com.vke.utils.Utils;

public class CodeLookupTable {
    private final Code[] lookup;
    private final int bitLength;

    public CodeLookupTable(Code[] codes, int bitLength) {
        this.bitLength = bitLength;
        int size = 1 << bitLength;
        lookup = new Code[size];

        for (Code code : codes) {
            // okay so basically we have to fill the lookup array with all possible variations of a code.
            // since the prefixes are unique, meaning that no code is a prefix of another one, we can simply
            // add all the remaining bit patterns.
            int len = code.codeLength();
            if (len == 0) {
                //unused
                continue;
            }
            if (len > bitLength) {
                //this code is too chonky and cannot be handled by this lookup table
                continue;
            }
            int lsbToMsbCode = Integer.reverse(code.code()) >>> (32 - len);
            int missingBitCopies = 1 << (bitLength - len);
            for (int i = 0; i < missingBitCopies; i++) {
                int index = lsbToMsbCode | (i << len);
                ////System.out.println("code: " + Utils.intToBinStr(lsbToMsbCode) + ", length: " + len + ", index: " + Utils.intToBinStr(index) + ", missing: " + missingBitCopies);
                if (Utils.verifyArrayIndex(index, lookup)) {
                    lookup[index] = code;
                } else {
                    System.err.println("Illegal array index found for lookup table! This should never happen!!!!!");
                }
            }
            ////System.out.println();
        }
    }

    /// bits are supposed in LSB order!
    public Code tryLookupCode(int bits) {
        return lookup[bits];
    }
}
