package com.vke.core.file.gzip.deflate.huffman;

import com.vke.core.file.gzip.deflate.BitUtils;
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
            if (len > bitLength) {
                //this code is too chonky and cannot be handled by this lookup table
                continue;
            }
            int lsbToMsbCode = Integer.reverse(code.code()) >>> (32 - len);
            int missingBits = bitLength - len;
            if (missingBits == 0) {
                //fast path
                lookup[lsbToMsbCode] = code;
                continue;
            }
            for (int i = 0; i < missingBits; i++) {
                int index = lsbToMsbCode | (i << code.codeLength());
                //System.out.println("code: " + Utils.intToBinStr(lsbToMsbCode) + ", length: " + len + ", index: " + Utils.intToBinStr(index) + ", missing: " + missingBits);
                if (Utils.verifyArrayIndex(index, lookup)) {
                    lookup[index] = code;
                } else {
                    System.err.println("Illegal array index found for lookup table! This should never happen!!!!!");
                }
            }
            //System.out.println();
        }
    }

    /// bits are supposed in LSB order!
    public Code tryLookupCode(int bits) {
        Code c = lookup[bits];
        if (c == null) return null;
        if (c.codeLength() == 0) return null;

        int len = c.codeLength();
        if (len > bitLength) {
            return null;
        }
        int actualCode = BitUtils.lowBits(bits, len);
        int rev = Integer.reverse(actualCode) >>> (32 - len);
        if (rev != c.code()) {
            return null;
        }
        return c;
    }
}
