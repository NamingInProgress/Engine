package com.vke.core.file.ogg.vorbis.header.setup;

import com.vke.core.file.deflate.decompress.BitUtils;
import com.vke.core.file.deflate.decompress.huffman.Code;
import com.vke.core.file.io.bit.BitStreamUtils;
import com.vke.core.file.io.bit.input.BitInputStream;
import com.vke.core.file.ogg.vorbis.Helpers;
import com.vke.core.file.ogg.vorbis.header.IdentHeader;
import com.vke.core.file.ogg.vorbis.VorbisStreamUndecodableException;
import com.vke.core.file.ogg.vorbis.header.setup.floor.Floor0;
import com.vke.core.file.ogg.vorbis.header.setup.floor.Floor1;
import com.vke.core.file.ogg.vorbis.header.setup.huffman.VorbisHMCodeGenerator;
import com.vke.utils.Utils;

import java.io.IOException;
import java.util.Arrays;

public class SetupHeader {
    private final IdentHeader ident;

    public final Codebook[] codebooks;
    public final FloorConfig[] floorConfigs;
    public final Residue[] residues;
    public final Mapping[] mappings;
    public final Mode[] modes;

    public SetupHeader(IdentHeader ident, BitInputStream bitStream) throws IOException {
        this.ident = ident;

        int codebookAmt = bitStream.readBits(8) + 1;
        this.codebooks = new Codebook[codebookAmt];
        for (int i = 0; i < codebookAmt; i++) {
            codebooks[i] = decodeCodebook(bitStream);
        }

        //These timecodes are appearently a reserved feature and are not used at all
        int timeCodeAmt = bitStream.readBits(6) + 1;
        for (int i = 0; i < timeCodeAmt; i++) {
            int v = bitStream.readBits(16);
            //if a value is somehow set, then we break out here cuz the stream is not decodable this way lmao
            if (v != 0) throw new VorbisStreamUndecodableException();
        }

        int floorAmt = bitStream.readBits(6) + 1;
        this.floorConfigs = new FloorConfig[floorAmt];
        for (int i = 0; i < floorAmt; i++) {
            floorConfigs[i] = decodeFloorConfig(bitStream);
        }

        int residueAmt = bitStream.readBits(6) + 1;
        this.residues = new Residue[residueAmt];
        for (int i = 0; i < residueAmt; i++) {
            residues[i] = decodeResidue(bitStream);
        }

        int mappingAmt = bitStream.readBits(6) + 1;
        this.mappings = new Mapping[mappingAmt];
        for (int i = 0; i < mappingAmt; i++) {
            mappings[i] = decodeMapping(bitStream);
        }

        int modesAmt = bitStream.readBits(6) + 1;
        this.modes = new Mode[modesAmt];
        for (int i = 0; i < modesAmt; i++) {
            modes[i] = decodeMode(bitStream);
        }
    }

    //https://xiph.org/vorbis/doc/Vorbis_I_spec.pdf#section.3
    private Codebook decodeCodebook(BitInputStream bitStream) throws IOException {
        int sync_0x564342 = bitStream.readBits(24);
        int dimensions = bitStream.readBits(16);
        int entries = bitStream.readBits(24);
        boolean ordered = BitStreamUtils.readFlag(bitStream);

        Codeword[] codewords = new Codeword[entries];
        if (!ordered) {
            boolean sparse = BitStreamUtils.readFlag(bitStream);
            for (int entry = 0; entry < entries; entry++) {
                boolean unused = false;
                int codewordLength = 0;
                if (sparse) {
                    boolean flag = BitStreamUtils.readFlag(bitStream);
                    if (flag) {
                        codewordLength = bitStream.readBits(5) + 1;
                    } else {
                        unused = true;
                    }
                } else {
                    codewordLength = bitStream.readBits(5) + 1;
                }

                codewords[entry] = new Codeword(codewordLength, unused);
            }
        } else {
            int currentEntry = 0;
            int currentLength = bitStream.readBits(5) + 1;
            while (currentEntry < entries) {
                int bitsToRead = Helpers.ilog(entries - currentEntry);
                int number = bitStream.readBits(bitsToRead);

                if (currentEntry + number > entries) {
                    throw new VorbisStreamUndecodableException("Ordered codebook allocation exceeded entry count.");
                }

                for (int i = 0; i < number; i++) {
                    codewords[currentEntry + i] = new Codeword(currentLength, false);
                }

                currentEntry += number;
                currentLength++;
            }
        }

        int lookupType = bitStream.readBits(4);

        if (lookupType > 2) {
            throw new VorbisStreamUndecodableException("Reserved codebook lookup type: " + lookupType);
        }

        Code[] actualCodes = VorbisHMCodeGenerator.generateVorbisCodes(codewords);

        if (lookupType == 0) {
            return new Codebook(actualCodes, dimensions, entries, lookupType);
        }

        float minVal = Helpers.float32_unpack(bitStream.readBits(32));
        float deltaVal = Helpers.float32_unpack(bitStream.readBits(32));
        int valBits = bitStream.readBits(4) + 1;
        boolean seqP = BitStreamUtils.readFlag(bitStream);

        int lookupValues = 0;
        if (lookupType == 1) {
            lookupValues = Helpers.lookup1_values(entries, dimensions);
        } else if (lookupType == 2) {
            lookupValues = entries * dimensions;
        }

        long[] multiplicands = new long[lookupValues];
        for (int i = 0; i < lookupValues; i++) {
            multiplicands[i] = bitStream.readBits(valBits);
        }

        return new Codebook(actualCodes, dimensions, entries, minVal, deltaVal, seqP, multiplicands, lookupType, lookupValues);
    }

    private FloorConfig decodeFloorConfig(BitInputStream bitStream) throws IOException {
        int floorType = bitStream.readBits(16);
        Object floor;
        if (floorType == 0) {
            int f0_order = bitStream.readBits(8);
            int f0_rate = bitStream.readBits(16);
            int f0_barkMapSize = bitStream.readBits(16);
            int f0_amplitudeBits = bitStream.readBits(6);
            int f0_amplitudeOffset = bitStream.readBits(8);
            int f0_numBooks = bitStream.readBits(4) + 1;
            int[] f0_bookList = new int[f0_numBooks];
            for (int i = 0; i < f0_numBooks; i++) {
                f0_bookList[i] = bitStream.readBits(8);
            }
            floor = new Floor0(f0_order, f0_rate, f0_barkMapSize, f0_amplitudeBits, f0_amplitudeOffset, f0_numBooks, f0_bookList);
        } else if (floorType == 1) {
            int partitions = bitStream.readBits(5);

            int[] partitionClassList = new int[partitions];
            int maximumClass = -1;
            for (int i = 0; i < partitions; i++) {
                partitionClassList[i] = bitStream.readBits(4);
                if (partitionClassList[i] > maximumClass) {
                    maximumClass = partitionClassList[i];
                }
            }

            int[] classDimensions = new int[maximumClass + 1];
            int[] classSubclasses = new int[maximumClass + 1];
            int[] classMasterbooks = new int[maximumClass + 1];
            int[][] subclassBooks = new int[maximumClass + 1][];

            for (int i = 0; i <= maximumClass; i++) {
                classDimensions[i] = bitStream.readBits(3) + 1;
                classSubclasses[i] = bitStream.readBits(2);

                if (classSubclasses[i] != 0) {
                    classMasterbooks[i] = bitStream.readBits(8);
                }

                int numSubclassBooks = 1 << classSubclasses[i];
                subclassBooks[i] = new int[numSubclassBooks];
                for (int j = 0; j < numSubclassBooks; j++) {
                    subclassBooks[i][j] = bitStream.readBits(8) - 1;
                }
            }

            int multiplier = bitStream.readBits(2) + 1;
            int rangeBits = bitStream.readBits(4);

            int totalXValues = 2;
            for (int i = 0; i < partitions; i++) {
                int classNum = partitionClassList[i];
                totalXValues += classDimensions[classNum];
            }

            int[] xList = new int[totalXValues];

            xList[0] = 0;
            xList[1] = 1 << rangeBits;

            int floor1Values = 2;

            for (int i = 0; i < partitions; i++) {
                int currentClassNumber = partitionClassList[i];
                for (int j = 0; j < classDimensions[currentClassNumber]; j++) {
                    xList[floor1Values] = bitStream.readBits(rangeBits);
                    floor1Values++;
                }
            }

            floor = new Floor1(
                    partitions, partitionClassList, classDimensions, classSubclasses,
                    classMasterbooks, subclassBooks, multiplier, rangeBits, xList
            );
        } else {
            throw new VorbisStreamUndecodableException("Illegal floor type");
        }

        return new FloorConfig(floorType, floor);
    }

    private Residue decodeResidue(BitInputStream bitStream) throws IOException {
        int type = bitStream.readBits(16);
        if (type > 2) throw new VorbisStreamUndecodableException("illegal residue type");
        int begin = bitStream.readBits(24);
        int end = bitStream.readBits(24);
        int partitionSize = bitStream.readBits(24) + 1;
        int classifications = bitStream.readBits(6) + 1;
        int classbookIdx = bitStream.readBits(8);
        if (!Utils.verifyArrayIndex(classbookIdx, codebooks)) throw new VorbisStreamUndecodableException();
        Codebook classbook = codebooks[classbookIdx];
        //the +1 is to account for rounding errors lol
        if (Math.pow(classifications, classbook.dimensions) > (classbook.entries + 1)) {
            throw new VorbisStreamUndecodableException();
        }
        int[] cascades = new int[classifications];
        for (int i = 0; i < classifications; i++) {
            int highBits = 0;
            int lowBits = bitStream.readBits(3);
            boolean flag = BitStreamUtils.readFlag(bitStream);
            if (flag) {
                highBits = bitStream.readBits(5);
            }
            cascades[i] = highBits * 8 + lowBits;
        }
        int[][] books = new int[classifications][8];
        for (int i = 0; i < classifications; i++) {
            for (int j = 0; j < 8; j++) {
                if (BitUtils.bitSet(cascades[i], j)) {
                    books[i][j] = bitStream.readBits(8);
                } else {
                    books[i][j] = -1;
                }
            }
        }
        return new Residue(type, begin, end, partitionSize, classifications, classbookIdx, cascades, books);
    }

    private Mapping decodeMapping(BitInputStream bitStream) throws IOException {
        int type = bitStream.readBits(16);
        if (type != 0) throw new VorbisStreamUndecodableException();
        boolean flag = BitStreamUtils.readFlag(bitStream);
        int mappingSubmaps = 1;
        if (flag) {
            mappingSubmaps = bitStream.readBits(4) + 1;
        }

        boolean flag2 = BitStreamUtils.readFlag(bitStream);
        int mappingCouplingSteps = 0;
        int[] mappingMagnitude = new int[0];
        int[] mappingAngle = new int[0];
        if (flag2) {
            mappingCouplingSteps = bitStream.readBits(8) + 1;
            mappingMagnitude = new int[mappingCouplingSteps];
            mappingAngle = new int[mappingCouplingSteps];
            int toRead = Helpers.ilog(ident.channels - 1);
            for (int i = 0; i < mappingCouplingSteps; i++) {
                mappingMagnitude[i] = bitStream.readBits(toRead);
                mappingAngle[i] = bitStream.readBits(toRead);

                if (mappingMagnitude[i] == mappingAngle[i] || mappingMagnitude[i] > ident.channels - 1 || mappingAngle[i] > ident.channels - 1) {
                    throw new VorbisStreamUndecodableException();
                }
            }
        }
        int reservedField = bitStream.readBits(2);
        if (reservedField != 0) {
            throw new VorbisStreamUndecodableException();
        }
        int[] mappingMux = new int[0];
        if (mappingSubmaps > 1) {
            mappingMux = new int[ident.channels];
            for (int i = 0; i < ident.channels; i++) {
                mappingMux[i] = bitStream.readBits(4);
                if (mappingMux[i] > mappingSubmaps - 1) {
                    throw new VorbisStreamUndecodableException();
                }
            }
        }

        int[] mappingSubmapFloor = new int[mappingSubmaps];
        int[] mappingSubmapResidue = new int[mappingSubmaps];
        for (int i = 0; i < mappingSubmaps; i++) {
            //unused timecode thingy from earlier
            bitStream.readBits(8);
            mappingSubmapFloor[i] = bitStream.readBits(8);
            if (!Utils.verifyArrayIndex(mappingSubmapFloor[i], floorConfigs)) throw new VorbisStreamUndecodableException();

            mappingSubmapResidue[i] = bitStream.readBits(8);
            if (!Utils.verifyArrayIndex(mappingSubmapResidue[i], residues)) throw new VorbisStreamUndecodableException();
        }

        return new Mapping(mappingSubmaps, mappingCouplingSteps, mappingMagnitude, mappingAngle, mappingMux, mappingSubmapFloor, mappingSubmapResidue);
    }

    private Mode decodeMode(BitInputStream bitStream) throws IOException {
        boolean blockFlag = BitStreamUtils.readFlag(bitStream);
        int windowType = bitStream.readBits(16);
        int transformType = bitStream.readBits(16);
        int mapping = bitStream.readBits(8);

        if (windowType != 0 || transformType != 0) {
            throw new VorbisStreamUndecodableException();
        }

        if (!Utils.verifyArrayIndex(mapping, mappings)) {
            throw new VorbisStreamUndecodableException();
        }

        return new Mode(blockFlag, windowType, transformType, mapping);
    }

    @Override
    public String toString() {
        return "SetupHeader{" +
                ", codebooks=" + Arrays.toString(codebooks) +
                ", floorConfigs=" + Arrays.toString(floorConfigs) +
                ", residues=" + Arrays.toString(residues) +
                ", mappings=" + Arrays.toString(mappings) +
                '}';
    }
}
