package com.vke.core.file.deflate.decompress.lz77;

import com.vke.core.file.deflate.decompress.check.Checksum32;
import com.vke.core.file.deflate.decompress.huffman.HMSymbolDecoder;
import com.vke.core.file.io.bit.input.BitInputStream;
import com.vke.core.file.io.bit.BitOrdering;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class Lz77Decoder {
    private final HMSymbolDecoder literalLengthDecoder;
    private final HMSymbolDecoder distanceDecoder;
    private final SlidingWindow slidingWindow;
    private boolean finished;
    private int pendingLength = 0;
    private int pendingDistance = 0;


    public Lz77Decoder(int[] literalCodeLengths, int[] distanceCodeLengths, SlidingWindow window) {
        this.literalLengthDecoder = new HMSymbolDecoder(literalCodeLengths, BitOrdering.LSB_FIRST);
        this.slidingWindow = window;
        this.finished = false;
        this.distanceDecoder = new HMSymbolDecoder(distanceCodeLengths, BitOrdering.LSB_FIRST);
    }

    public int decodeNextByte(BitInputStream bitStream) throws IOException {
        if (finished) {
            return -1;
        }

        // Continue an active LZ copy
        if (pendingLength > 0) {
            byte b = slidingWindow.backtrack(pendingDistance);
            slidingWindow.append(b);
            pendingLength--;
            return b & 0xFF;
        }

        bitStream.setOrdering(BitOrdering.LSB_FIRST);
        int symbol = literalLengthDecoder.decodeSymbol(bitStream);

        if (symbol < 0) {
            throw new IOException("Illegal symbol returned: " + symbol);
        }

        if (symbol <= 255) {
            //byte literal
            byte b = (byte) symbol;
            slidingWindow.append(b);
            return symbol;
        }

        if (symbol == 256) {
            //end of block
            finished = true;
            return -1;
        }

        //all easy symbols are done, the following combinations can only be length/distance pairs
        //the length comes first and distance second.
        //the distance is the offset backwards in the sliding window and lenght is how many bytes
        //length > distance is allowed, which will copy the bytes. therefore we need
        //to decode the bytes one by one and not use an array copy function

        //the way length codes work is that they have a base length and extra bits.
        //the complete length is baseLength + extraBits

        //length first
        int arrayKey = symbol - 257;
        int baseLength = Lz77Consts.LENGTH_CODES_BASE[arrayKey];
        int extraBitsAmount = Lz77Consts.LENGTH_CODES_BITS[arrayKey];
        int extraBits = bitStream.readBits(extraBitsAmount);
        int length = baseLength + extraBits;

        //read next symbol for distance. we can be sure that there exists a distance symbol here, cuz
        //thats the specification of deflate lz77

        //same for distance, but this time the symbol starts at 0
        //int distArrayKey = bitStream.readBits(5);
        int distArrayKey = distanceDecoder.decodeSymbol(bitStream);
        int baseDistance = Lz77Consts.DIST_CODES_BASE[distArrayKey];
        int distExtraBitsAmt = Lz77Consts.DIST_CODES_BITS[distArrayKey];
        int distExtraBits = bitStream.readBits(distExtraBitsAmt);
        int distance = baseDistance + distExtraBits;

        //System.out.println("LZ77");

        pendingLength = length - 1;
        pendingDistance = distance;

        byte b = slidingWindow.backtrack(pendingDistance);
        slidingWindow.append(b);

        return b & 0xFF;

    }

    public final int decodeNextBytes(BitInputStream bitStream, byte[] dst, int start, int length, @Nullable Checksum32 checksum) throws IOException {
        if (finished) {
            return -1;
        }
        int totalRead = 0;
        outer:
        while (totalRead < length) {
            // Continue an active LZ copy
            if (pendingLength > 0) {
                //how many bytes will be handled with this call
                int handleLength = Math.min(pendingLength, length - totalRead);
                if (pendingDistance < handleLength) {
                    //manual loop here because lz allows copying bytes that arent yet in the sliding window
                    for (int i = 0; i < handleLength; i++) {
                        byte b = slidingWindow.backtrack(pendingDistance);
                        slidingWindow.append(b);
                        if (checksum != null) checksum.nextByte(b);
                        dst[start + totalRead + i] = b;
                    }
                } else {
                    //the slice to copy is fully in the sliding window, so we can use a fast version
                    slidingWindow.fastBacktrackAndAppend(dst, start + totalRead, handleLength, pendingDistance, checksum);
                }
                totalRead += handleLength;
                pendingLength -= handleLength;
            }
            if (totalRead == length) {
                return length;
            }

            bitStream.setOrdering(BitOrdering.LSB_FIRST);
            for (int n = totalRead; n < length; n++) {
                int symbol = literalLengthDecoder.decodeSymbol(bitStream);

                if (symbol < 0) {
                    throw new IOException("Illegal symbol returned: " + symbol);
                }

                if (symbol <= 255) {
                    //byte literal
                    byte b = (byte) symbol;
                    slidingWindow.append(b);
                    if (checksum != null) checksum.nextByte(b);
                    dst[start + n] = b;
                    continue;
                }

                if (symbol == 256) {
                    //end of block
                    finished = true;
                    return n;
                }

                //all easy symbols are done, the following combinations can only be length/distance pairs
                //the length comes first and distance second.
                //the distance is the offset backwards in the sliding window and lenght is how many bytes
                //length > distance is allowed, which will copy the bytes. therefore we need
                //to decode the bytes one by one and not use an array copy function

                //the way length codes work is that they have a base length and extra bits.
                //the complete length is baseLength + extraBits

                //length first
                int arrayKey = symbol - 257;
                int baseLength = Lz77Consts.LENGTH_CODES_BASE[arrayKey];
                int extraBitsAmount = Lz77Consts.LENGTH_CODES_BITS[arrayKey];
                int extraBits = bitStream.readBits(extraBitsAmount);
                int lzLength = baseLength + extraBits;

                //read next symbol for distance. we can be sure that there exists a distance symbol here, cuz
                //thats the specification of deflate lz77

                //same for distance, but this time the symbol starts at 0
                //int distArrayKey = bitStream.readBits(5);
                int distArrayKey = distanceDecoder.decodeSymbol(bitStream);
                int baseDistance = Lz77Consts.DIST_CODES_BASE[distArrayKey];
                int distExtraBitsAmt = Lz77Consts.DIST_CODES_BITS[distArrayKey];
                int distExtraBits = bitStream.readBits(distExtraBitsAmt);
                int distance = baseDistance + distExtraBits;

                //System.out.println("LZ77");

                pendingLength = lzLength;
                pendingDistance = distance;

                totalRead = n;
                continue outer;
            }
            totalRead = length;
        }
        return totalRead;
    }

    public boolean isFinished() {
        return finished;
    }
}
