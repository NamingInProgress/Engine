package com.vke.core.file.deflate.compress.huffman;

import com.vke.core.file.deflate.decompress.block.FixedBlock;
import com.vke.core.file.deflate.decompress.huffman.Code;
import com.vke.core.file.deflate.decompress.huffman.HMSymbolDecoder;

import java.util.ArrayList;
import java.util.List;

public class HuffmanCodeGenerator {
    public static Code[] generateCodesFromFrequencies(int[] frequencies, int maxLength) {
        int[] codeLengths = PackageMerge.perform(frequencies, maxLength);
        return HMSymbolDecoder.createCodesFromLengths(codeLengths);
    }

    public static Code[] generateCodesFromFrequencies(int[] frequencies) {
        int[] codeLengths = PackageMerge.perform(frequencies, 15);
        return HMSymbolDecoder.createCodesFromLengths(codeLengths);
    }

    public static Code[] getFixedLitLenCodes() {
        int[] lengths = FixedBlock.LITERAL_LENGTH_CODE_LENGTHS;
        return HMSymbolDecoder.createCodesFromLengths(lengths);
    }

    public static Code[] getFixedDistanceCodes() {
        int[] lengths = FixedBlock.DISTANCE_CODE_LENGTHS;
        return HMSymbolDecoder.createCodesFromLengths(lengths);
    }

    public static CodeLengthCode[] generateCodeLengthCodes(int[] codeLengths) {
        int educatedGuess = codeLengths.length / 2;
        ArrayList<CodeLengthCode> output = new ArrayList<>(educatedGuess); //im famous for my educated guesses
        int[] frequencies = new int[19]; //codes available

        int repeatCounter = 0;
        int repeatLength = 0;

        //step 1: convert lengths to symbols here
        for (int i = 0; i < codeLengths.length; i++) {
            int len = codeLengths[i];

            if (repeatLength == len) {
                //this is a run!
                repeatCounter++;
            } else {
                if (repeatCounter > 0) {
                    emitSequence(output, frequencies, repeatLength, repeatCounter);
                    repeatCounter = 0;
                }
                emitSingle(output, frequencies, len);
                repeatLength = len;
            }
        }

        if (repeatCounter > 0) {
            emitSequence(output, frequencies, repeatLength, repeatCounter);
        }

        //step 2: generate the codeLengthCodeLengths using package-merge
        Code[] codeLengthCodeLengthCodes = generateCodesFromFrequencies(frequencies, 7);
        CodeLengthCode[] array = output.toArray(CodeLengthCode[]::new);
        for (CodeLengthCode c : array) {
            int symbol = c.symbol;
            Code code = codeLengthCodeLengthCodes[symbol];
            c.code = code.code();
            c.codeLength = code.codeLength();
        }

        return array;
    }

    private static void emitSequence(ArrayList<CodeLengthCode> out, int[] frequencies, int length, int repeat) {
        ////System.out.println("SEQUENCE: " + length + " x " + repeat);
        if (length == 0) {
            //emit the cool 0s codes here
            if (repeat >= 3 && repeat <= 10) {
                //code 17 for 3-10 times
                int extraBits = repeat - 3;
                out.add(new CodeLengthCode(17, -1, -1, extraBits));
                frequencies[17]++;
            } else if (repeat > 10) {
                //code 18 for 11-138 times
                int remaining = repeat;
                while (remaining > 0) {
                    int extraBits = Math.min(remaining, 138) - 11;
                    out.add(new CodeLengthCode(18, -1, -1, extraBits));
                    frequencies[18]++;

                    remaining -= 138;
                }
            } else {
                //booooring 0s here as well
                for (int i = 0; i < repeat; i++) {
                    emitSingle(out, frequencies, 0);
                }
            }
        } else {
            if (repeat >= 3) {
                //emit cool repeat code
                if (repeat > 6) {
                    //do it multiple times
                    int remaining = repeat;
                    while (remaining > 0) {
                        int extraBits = Math.min(remaining, 6) - 3;
                        out.add(new CodeLengthCode(16, -1, -1, extraBits));
                        frequencies[16]++;

                        remaining -= 6;
                    }
                } else {
                    //once is enough
                    int extraBits = repeat - 3;
                    out.add(new CodeLengthCode(16, -1, -1, extraBits));
                    frequencies[16]++;
                }
            } else {
                //emit boring normal codes
                for (int i = 0; i < repeat; i++) {
                    emitSingle(out, frequencies, length);
                }
            }
        }
    }

    private static void emitSingle(ArrayList<CodeLengthCode> out, int[] frequencies, int length) {
        ////System.out.println("SINGLE: " + length);
        CodeLengthCode c = new CodeLengthCode(length, -1, -1, 0);
        frequencies[length]++;
        out.add(c);
    }

    public static final class CodeLengthCode {
        private final int symbol;
        private final int extraBits;
        private int code;
        private int codeLength;

        public CodeLengthCode(int symbol, int code, int codeLength, int extraBits) {
            this.symbol = symbol;
            this.code = code;
            this.codeLength = codeLength;
            this.extraBits = extraBits;
        }

        public int symbol() {
            return symbol;
        }

        public int code() {
            return code;
        }

        public int codeLength() {
            return codeLength;
        }

        public int extraBits() {
            return extraBits;
        }
    }
}
