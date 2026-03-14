package com.vke.core.file.deflate.decompress.huffman;

import com.vke.core.file.deflate.decompress.BitUtils;
import com.vke.core.file.io.bit.input.BitInputStream;
import com.vke.core.file.io.bit.BitOrdering;

import java.io.IOException;

public class HMSymbolDecoder {
    public static final int MAX_CODE_LENGTH = 15;
    private static final int FAST_BITS = 8;
    private static final int SLOW_BITS = 12;
    
    private final int[] codeLengths;
    private final Code[] codes;
    private final CodeLookupTable lowerTable;
    private final CodeLookupTable upperTable;
    private final int maxUsedCodeLength;
    
    public HMSymbolDecoder(int[] codeLengths) {
        this.codeLengths = codeLengths;
        this.codes = createCodesFromLengths(codeLengths);

        //in theory, the smaller lower table will fit into the cpu L1 cache and is much much faster than
        //the bigger upper table
        this.lowerTable = new CodeLookupTable(codes, FAST_BITS);
        this.upperTable = new CodeLookupTable(codes, SLOW_BITS);
        
        int maxUsedCodeLength = 0;
        for (int len : codeLengths) {
            maxUsedCodeLength = Math.max(maxUsedCodeLength, len);
        }
        this.maxUsedCodeLength = maxUsedCodeLength;
    }
    
    public static Code[] createCodesFromLengths(int[] codeLengths) {
        //see: https://www.nayuki.io/page/deflate-specification-v1-3-html#section-3-2-2
        Code[] codes = new Code[codeLengths.length];
        int[] blCount = new int[MAX_CODE_LENGTH + 1];
        for (int length : codeLengths) {
            if (length > 0) {
                blCount[length]++;
            }
        }

        int[] nextCode = new int[MAX_CODE_LENGTH + 1];
        int code = 0;
        blCount[0] = 0;
        for (int bits = 1; bits <= MAX_CODE_LENGTH; bits++) {
            code = (code + blCount[bits - 1]) << 1;
            nextCode[bits] = code;
        }

        for (int i = 0; i < codeLengths.length; i++) {
            int len = codeLengths[i];
            if (len != 0) {
                int rawCode = nextCode[len];
                //fix bit ordering cuz for some reason huffman codes are reversed here lol
                //thats what the specification says
                codes[i] = new Code(rawCode, len, i);
                nextCode[len]++;
            } else {
                codes[i] = new Code(0, 0, i);
            }
        }

        return codes;
    }

    public int decodeSymbol(BitInputStream bitStream) throws IOException {
        bitStream.setOrdering(BitOrdering.LSB_FIRST);
        int nextBits = bitStream.peekBits(maxUsedCodeLength);
        int fastBits = BitUtils.lowBits(nextBits, FAST_BITS);
        Code tried = lowerTable.tryLookupCode(fastBits);

        if (tried == null) {
            int slowBits = BitUtils.lowBits(nextBits, SLOW_BITS);
            tried = upperTable.tryLookupCode(slowBits);
        }
        if (tried == null) {
            //since we didnt match the bits to any of the entries in the lookup tables,
            //we have to "walk the tree" manually here
            //instead of reconstructing the large tree, we can simply read 1 bit
            //at a time and keep track of them in a prefix int and check if any symbol matches.
            //this is super slow, but these long codes are super rare
            bitStream.setOrdering(BitOrdering.LSB_FIRST);
            int prefix = bitStream.readBits(1);
            outer:
            for (int i = 1; i <= maxUsedCodeLength; i++) {
                for (Code sc : codes) {
                    //if (true) break;
                    if (sc.codeLength() == 0) continue;
                    if (sc.codeLength() == i && sc.code() == prefix) {
                        tried = sc;
                        break outer;
                    }
                }
                int nextBit = bitStream.readBits(1);
                prefix = (prefix << 1) | nextBit;
            }
            //System.out.println(BitUtils.intToBinStr(prefix) + " len " + maxUsedCodeLength);
        } else {
            int len = tried.codeLength();
            bitStream.readBits(len);
        }
        if (tried == null) {
            throw new IOException("Unable to match code to symbol: " + BitUtils.intToBinStr(nextBits));
        }
        //System.out.println("decoded symbol: " + tried.symbol() + ", as char: " + ((char) tried.symbol()) + ", using code: " + BitUtils.intToBinStr(tried.code()) + ", len: " + tried.codeLength());
        return tried.symbol();
    }
}
