package com.vke.core.file.deflate.compress;

import com.vke.core.file.deflate.compress.huffman.HuffmanCodeGenerator;
import com.vke.core.file.deflate.compress.lz77.Lz77Symbol;
import com.vke.core.file.deflate.compress.lz77.SlidingWindow;
import com.vke.core.file.deflate.decompress.BitUtils;
import com.vke.core.file.deflate.decompress.block.DynamicBlock;
import com.vke.core.file.deflate.decompress.block.FixedBlock;
import com.vke.core.file.deflate.decompress.huffman.Code;
import com.vke.core.file.deflate.decompress.lz77.Lz77Consts;
import com.vke.core.file.io.bit.BitOrdering;
import com.vke.core.file.io.bit.input.ShittyBitInputStream;
import com.vke.core.file.io.bit.output.BitOutputStream;
import com.vke.core.file.io.bit.output.GoodBitOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class BlockBuilder {
    private final byte[] internalBuffer;
    private final int sizeThreshold;
    private int cursor;

    private final SlidingWindow slidingWindow;
    private final int maxChainChecks;


    public BlockBuilder(int sizeThreshold, int slidingWindowSize, int maxChainChecks) {
        this.sizeThreshold = sizeThreshold;
        this.internalBuffer = new byte[sizeThreshold];
        this.slidingWindow = new SlidingWindow(slidingWindowSize);
        this.maxChainChecks = maxChainChecks;
    }

    public void onNextBytes(byte[] bytes, int start, int length, BitOutputStream stream) throws IOException {
        int offset = start;
        int remaining = length;

        while (remaining > 0) {
            int space = sizeThreshold - cursor;
            int toCopy = Math.min(space, remaining);
            System.arraycopy(bytes, offset, internalBuffer, cursor, toCopy);

            cursor += toCopy;
            offset += toCopy;
            remaining -= toCopy;
            if (cursor == sizeThreshold) {
                flushBlock(stream, false);
                cursor = 0;
            }
        }
    }

    public void flushBlock(BitOutputStream masterStream, boolean isFinalBlock) throws IOException {
        //in here i have to construct a dynamic block, a fixed block and see if their size exceeds the internalBuffer itself.
        //if so, use the uncompressed version. first collect all the symbols for the internal buffer with lz77
        int length = cursor;
        int symbolIndex = 0;

        //also count frequencies HERE for optimization
        int[] litlenFreq = new int[FixedBlock.LITERAL_LENGTH_CODE_LENGTHS.length];
        litlenFreq[256]++;

        int[] distFreq = new int[FixedBlock.DISTANCE_CODE_LENGTHS.length];

        ArrayList<Lz77Symbol> lz77Output = new ArrayList<>(length);
        int wouldveBeenE = 0;
        while (symbolIndex < cursor) {
            long lz77Symbol = slidingWindow.nextSymbol(maxChainChecks, internalBuffer, symbolIndex, length);
            if ((lz77Symbol & 0x8000000000000000L) != 0) {
                //MSB is set so this is a length/distance pair!
                int distanceCode = (int) (lz77Symbol & 0xFFFFFFFFL);
                int lengthCode = (int) ((lz77Symbol >>> 32) & 0x7FFFFFFFL);

                for (int i = 0; i < lengthCode; i++) {
                    //process all the original input bytes so that the hash chains inside the window
                    //are correctly updated
                    byte p = internalBuffer[symbolIndex + i];
                    slidingWindow.processByte(p);
                }

                int lenSymbol = getLengthSymbol(lengthCode);
                int distSymbol = getDistanceSymbol(distanceCode);
                litlenFreq[lenSymbol]++;
                distFreq[distSymbol]++;

                lz77Output.add(new Lz77Symbol(false, (byte) 0, lengthCode, distanceCode));

                symbolIndex += lengthCode;
            } else {
                //MSB is not set so this is a literal symbol
                byte byteLiteral = (byte) (lz77Symbol & 0xFF);
                slidingWindow.processByte(byteLiteral);
                lz77Output.add(new Lz77Symbol(true, byteLiteral & 0xFF, 0, 0));
                litlenFreq[byteLiteral & 0xFF]++;
                symbolIndex++;
            }
        }

        //with these symbols obtained, we can actually build the huffman trees now!
        //start with fixed, as it is the easiest of both

        int uncompressedSize = cursor + 1; //the +1 is for byte alignment

        BlockResult fixedResult = buildFixedBlock(lz77Output);
        //then dynamic over here
        BlockResult dynamicResult = buildDynamicBlock(lz77Output, litlenFreq, distFreq);

        boolean dynamicIsTheWinner = false;
        if (fixedResult.fullBytes() == dynamicResult.fullBytes()) {
            dynamicIsTheWinner = fixedResult.partialBits() > dynamicResult.partialBits();
        } else {
            dynamicIsTheWinner = fixedResult.fullBytes() > dynamicResult.fullBytes();
        }
        dynamicIsTheWinner = false;

        boolean useUncompressed = false;
        if (dynamicIsTheWinner) {
            useUncompressed = uncompressedSize < dynamicResult.fullBytes();
        } else {
            useUncompressed = uncompressedSize < fixedResult.fullBytes();
        }
        useUncompressed = false;

        if (useUncompressed) {
            masterStream.setOrdering(BitOrdering.LSB_FIRST);
            writeBlockHeader(masterStream, 0, isFinalBlock);
            masterStream.alignToByte();
            int LEN = cursor;
            int NLEN = ~LEN;
            masterStream.writeBits(LEN, 16);
            masterStream.writeBits(NLEN, 16);
            masterStream.streamDirectAligned(internalBuffer, 0, cursor);
        } else {
            ByteArrayOutputStream bao;
            int full, partial;
            if (dynamicIsTheWinner) {
                writeBlockHeader(masterStream, 2, isFinalBlock);
                bao = dynamicResult.byteOutput();
                full = dynamicResult.fullBytes();
                partial = dynamicResult.partialBits();
            } else {
                writeBlockHeader(masterStream, 1, isFinalBlock);
                bao = fixedResult.byteOutput();
                full = fixedResult.fullBytes();
                partial = fixedResult.partialBits();
            }
            //write the bao of the blockresult together with the partial bits that exist
            byte[] rawData = bao.toByteArray();
            for (int i = 0; i < full; i++) {
                masterStream.writeBits(rawData[i], 8);
            }
            if (partial > 0) {
                int partialByte = rawData[full];
                masterStream.writeBits(partialByte, partial);
            }
        }
    }

    private void writeBlockHeader(BitOutputStream masterStream, int type, boolean bfinal) throws IOException {
        masterStream.writeBits(bfinal ? 1 : 0, 1);
        masterStream.writeBits(type, 2);
    }

    public BlockResult buildFixedBlock(ArrayList<Lz77Symbol> symbols) throws IOException {
        Code[] literalLengthCodes = HuffmanCodeGenerator.getFixedLitLenCodes();
        Code[] distanceCodes = HuffmanCodeGenerator.getFixedDistanceCodes();

        ByteArrayOutputStream bao = new ByteArrayOutputStream(sizeThreshold / 2); //educated guess lol
        BitOutputStream bitOutputStream = new GoodBitOutputStream(bao);
        bitOutputStream.setOrdering(BitOrdering.LSB_FIRST);

        return buildBlockFromCodes(symbols, literalLengthCodes, distanceCodes, bitOutputStream, bao);
    }

    public BlockResult buildDynamicBlock(ArrayList<Lz77Symbol> symbols, int[] litlenFreq, int[] distFreq) throws IOException {
        Code[] litlenCodes = HuffmanCodeGenerator.generateCodesFromFrequencies(litlenFreq);
        System.out.println("===== CODES LIT LEN =====");
        for (Code c : litlenCodes) {
            if (c.codeLength() != 0) {
                System.out.println(c);
            }
        }
        System.out.println("=================");
        Code[] distCodes = HuffmanCodeGenerator.generateCodesFromFrequencies(distFreq);

        ByteArrayOutputStream bao = new ByteArrayOutputStream(sizeThreshold / 2); //educated guess lol
        GoodBitOutputStream bitOutputStream = new GoodBitOutputStream(bao);
        bitOutputStream.setOrdering(BitOrdering.LSB_FIRST);

        int[] litLenCodeLengths = Arrays.stream(litlenCodes).mapToInt(Code::codeLength).toArray();
        int[] distanceCodeLengths = Arrays.stream(distCodes).mapToInt(Code::codeLength).toArray();

        int total = litLenCodeLengths.length + distanceCodeLengths.length;
        int[] combinedLengths = new int[total];

        int litCount = litLenCodeLengths.length;
        while (litCount >= 257 && litLenCodeLengths[litCount - 1] == 0) {
            litCount--;
        }
        //System.out.println("HLIT = " + litCount);
        int HLIT = litCount - 257;

        int distCount = distanceCodeLengths.length;
        while (distCount >= 1 && distanceCodeLengths[distCount - 1] == 0) {
            distCount--;
        }
        //System.out.println("HDIST = " + distCount);
        int HDIST = distCount - 1;

        int totalRLESymbols = litCount + distCount;

        System.arraycopy(litLenCodeLengths, 0, combinedLengths, 0, litCount);
        System.arraycopy(distanceCodeLengths, 0, combinedLengths, litCount, distCount);

        HuffmanCodeGenerator.CodeLengthCode[] rleCodes = HuffmanCodeGenerator.generateCodeLengthCodes(combinedLengths);

        int[] order = DynamicBlock.CODE_LENGTH_ORDER;
        int[] codeLenCodeLengths = new int[19];

        for (HuffmanCodeGenerator.CodeLengthCode c : rleCodes) {
            //duplicate symbols will have the same length, this is just my way of passing it from the build frunction to here lol
            codeLenCodeLengths[c.symbol()] = c.codeLength();
        }

        //System.out.println("CODE LEN CODE LENGTHS:");
        //System.out.println(Arrays.toString(codeLenCodeLengths));

        int hclenCount = 19;
        while (hclenCount >= 4 && codeLenCodeLengths[order[hclenCount - 1]] == 0) {
            hclenCount--;
        }
        //System.out.println("HCLEN = " + hclenCount);
        int HCLEN = hclenCount - 4;

        bitOutputStream.writeBits(HLIT, 5);
        bitOutputStream.writeBits(HDIST, 5);
        bitOutputStream.writeBits(HCLEN, 4);

        BitOrdering ordering = bitOutputStream.getOrdering();
        bitOutputStream.setOrdering(BitOrdering.LSB_FIRST);
        for (int i = 0; i < 4 + HCLEN; i++) {
            int reorderedIndex = order[i];
            int bits = codeLenCodeLengths[reorderedIndex];
            bitOutputStream.writeBits(bits, 3);
        }

        //System.out.println("MAX SYMBOLS ALLOWED: " + totalRLESymbols);
        int symCounter = 0;
        for (int index = 0; index < rleCodes.length && symCounter < totalRLESymbols; index++) {
            HuffmanCodeGenerator.CodeLengthCode c = rleCodes[index];
            bitOutputStream.setOrdering(BitOrdering.MSB_FIRST);
            bitOutputStream.writeBits(c.code(), c.codeLength());
            bitOutputStream.setOrdering(BitOrdering.LSB_FIRST);
            switch (c.symbol()) {
                case 16 -> {
                    bitOutputStream.writeBits(c.extraBits(), 2);
                    symCounter += c.extraBits() + 3;
                }
                case 17 -> {
                    bitOutputStream.writeBits(c.extraBits(), 3);
                    symCounter += c.extraBits() + 3;
                }
                case 18 -> {
                    bitOutputStream.writeBits(c.extraBits(), 7);
                    symCounter += c.extraBits() + 11;
                }
                default -> {
                    symCounter += 1;
                }
            }
        }
        bitOutputStream.setOrdering(ordering);

        //System.out.println("ALL:");
        //System.out.println(Arrays.toString(combinedLengths));

        //System.out.println("LIT LEN CODES:");
        //System.out.println(Arrays.toString(litLenCodeLengths));

        //System.out.println("DIST CODES:");
        //System.out.println(Arrays.toString(distanceCodeLengths));

        BlockResult e = buildBlockFromCodes(symbols, litlenCodes, distCodes, bitOutputStream, bao);
        return e;
    }

    private BlockResult buildBlockFromCodes(ArrayList<Lz77Symbol> symbols, Code[] literalLengthCodes, Code[] distanceCodes, BitOutputStream bitOutputStream, ByteArrayOutputStream bao) throws IOException {
        for (Lz77Symbol symbol : symbols) {
            if (symbol.isLiteral()) {
                int value = symbol.literal() & 0xFF;
                Code literalCode = literalLengthCodes[value];
                Code tried = literalCode;
                //System.out.println("encoded symbol: " + tried.symbol() + ", as char: " + ((char) tried.symbol()) + ", using code: " + BitUtils.intToBinStr(tried.code()) + ", len: " + tried.codeLength());
                BitOrdering ordering = bitOutputStream.getOrdering();
                bitOutputStream.setOrdering(BitOrdering.MSB_FIRST);
                bitOutputStream.writeBits(literalCode.code(), literalCode.codeLength());
                bitOutputStream.setOrdering(ordering);
            } else {
                int len = symbol.length();
                int dist = symbol.distance();
                writeLengthDistance(bitOutputStream, len, dist, literalLengthCodes, distanceCodes);
            }
        }

        Code endCode = literalLengthCodes[256];
        bitOutputStream.setOrdering(BitOrdering.MSB_FIRST);
        bitOutputStream.writeBits(endCode.code(), endCode.codeLength());
        //System.out.println("encoded symbol: " + endCode.symbol() + ", as char: " + ((char) endCode.symbol()) + ", using code: " + BitUtils.intToBinStr(endCode.code()) + ", len: " + endCode.codeLength());

        int partialBits = bitOutputStream.partialBits();
        bitOutputStream.flushBuffer();
        int fullBytes = partialBits > 0 ? bao.size() -1 : bao.size();

        return new BlockResult(bao, partialBits, fullBytes);
    }

    private int getLengthSymbol(int length) {
        for (int i = 0; i < Lz77Consts.LENGTH_CODES_BASE.length; i++) {
            int base = Lz77Consts.LENGTH_CODES_BASE[i];
            int nextBase = (i < Lz77Consts.LENGTH_CODES_BASE.length - 1)
                    ? Lz77Consts.LENGTH_CODES_BASE[i + 1]
                    : 259; //the max is 258 for some reason

            if (length >= base && length < nextBase) {
                return 257 + i;
            }
        }
        throw new IllegalArgumentException("Invalid length: " + length);
    }

    private int getDistanceSymbol(int distance) {
        for (int i = 0; i < Lz77Consts.DIST_CODES_BASE.length; i++) {
            int base = Lz77Consts.DIST_CODES_BASE[i];
            int nextBase = (i < Lz77Consts.DIST_CODES_BASE.length - 1)
                    ? Lz77Consts.DIST_CODES_BASE[i + 1]
                    : Integer.MAX_VALUE;

            if (distance >= base && distance < nextBase) {
                return i;
            }
        }
        throw new IllegalArgumentException("Invalid distance: " + distance);
    }

    private void writeLengthDistance(BitOutputStream out, int length, int distance, Code[] litLenCodes, Code[] distCodes) throws IOException {
        int lenSym = 0;
        int extraBits = 0;
        int extraValue = 0;

        //collect the symbol itself and also the extra bits amount!
        for (int i = 0; i < Lz77Consts.LENGTH_CODES_BASE.length; i++) {
            int base = Lz77Consts.LENGTH_CODES_BASE[i];
            int nextBase = (i < Lz77Consts.LENGTH_CODES_BASE.length - 1) ? Lz77Consts.LENGTH_CODES_BASE[i + 1] : 259;
            if (length >= base && length < nextBase) {
                lenSym = i + 257;
                extraBits = Lz77Consts.LENGTH_CODES_BITS[i];
                extraValue = length - base;
                break;
            }
        }

        Code lenCode = litLenCodes[lenSym];
        Code tried = lenCode;
        //System.out.println("encoded LENGTH: " + length + ", using symbol: " + lenSym + ", using code: " + BitUtils.intToBinStr(tried.code()));

        BitOrdering ordering = out.getOrdering();
        out.setOrdering(BitOrdering.MSB_FIRST);
        out.writeBits(lenCode.code(), lenCode.codeLength());

        if (extraBits > 0) {
            out.setOrdering(BitOrdering.LSB_FIRST);
            out.writeBits(extraValue, extraBits);
            out.setOrdering(BitOrdering.MSB_FIRST);
        }

        int distSym = 0;
        int distExtraBits = 0;
        int distExtraValue = 0;
        //symbol and extra bits for dist as well
        for (int i = 0; i < Lz77Consts.DIST_CODES_BASE.length; i++) {
            int base = Lz77Consts.DIST_CODES_BASE[i];
            int nextBase = (i < Lz77Consts.DIST_CODES_BASE.length - 1) ? Lz77Consts.DIST_CODES_BASE[i + 1] : Integer.MAX_VALUE;
            if (distance >= base && distance < nextBase) {
                distSym = i;
                distExtraBits = Lz77Consts.DIST_CODES_BITS[i];
                distExtraValue = distance - base;
                break;
            }
        }

        Code distCode = distCodes[distSym];
        tried = distCode;
        //System.out.println("encoded DISTANCE: " + distance + ", using symbol: " + distSym + ", using code: " + BitUtils.intToBinStr(tried.code()));
        out.writeBits(distCode.code(), distCode.codeLength());

        if (distExtraBits > 0) {
            out.setOrdering(BitOrdering.LSB_FIRST);
            out.writeBits(distExtraValue, distExtraBits);
            out.setOrdering(BitOrdering.MSB_FIRST);
        }

        out.setOrdering(ordering);
    }
}
