package com.vke.core.file.ogg.vorbis.data.floor;

import com.carrotsearch.hppc.ByteArrayList;
import com.carrotsearch.hppc.FloatArrayList;
import com.vke.core.file.io.bit.input.BitInputStream;
import com.vke.core.file.ogg.vorbis.Helpers;
import com.vke.core.file.ogg.vorbis.VorbisStreamUndecodableException;
import com.vke.core.file.ogg.vorbis.header.setup.Codebook;
import com.vke.core.file.ogg.vorbis.header.setup.FloorConfig;
import com.vke.core.file.ogg.vorbis.header.setup.floor.Floor0;
import com.vke.core.file.ogg.vorbis.header.setup.floor.Floor1;
import com.vke.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class FloorCurveData {
    private final BitInputStream bitStream;
    private final FloorCurveDecoder d;
    public final int ch;

    private final int submapNum;
    private final FloorConfig floor;

    public Floor0Data floor0Data;
    public Floor1Data floor1Data;

    public FloorCurveData(FloorCurveDecoder d, int ch) throws IOException {
        this.bitStream = d.bitStream;
        this.d = d;
        this.ch = ch;

        if (d.mapping.submasks() > 1 && d.mapping.mux() != null && d.mapping.mux().length > 0) {
            this.submapNum = d.mapping.mux()[ch];
        } else {
            this.submapNum = 0;
        }

        int floorNum = d.mapping.floor()[submapNum];
        this.floor = d.setup.floorConfigs[floorNum];
        int floorType = floor.type;
        if (floorType == 0) {
            decodeFloor0();
        } else if (floorType == 1) {
            decodeFloor1();
        }
    }

    private void decodeFloor0() throws IOException {
        Floor0 floor0 = (Floor0) floor.floor;
        int amplitude = bitStream.readBits(floor0.f0_amplitudeBits());

        if (amplitude > 0) {
            FloatArrayList coefficients = new FloatArrayList();

            int bookNumBits = Helpers.ilog(floor0.f0_numBooks());
            int bookNum = bitStream.readBits(bookNumBits);

            if (bookNum >= floor0.f0_numBooks()) {
                throw new VorbisStreamUndecodableException("Floor0 book index out of bounds.");
            }

            int actualCodebookIdx = floor0.f0_bookList()[bookNum];
            if (!Utils.verifyArrayIndex(actualCodebookIdx, d.setup.codebooks)) {
                throw new VorbisStreamUndecodableException("Floor0 references invalid codebook slot.");
            }

            Codebook targetBook = d.setup.codebooks[actualCodebookIdx];

            float last = 0.0f;
            while (coefficients.size() < floor0.f0_order()) {
                float[] tempVector = targetBook.decodeVQ(bitStream);

                for (float v : tempVector) {
                    float accumulatedVal = v + last;
                    coefficients.add(accumulatedVal);
                }

                if (tempVector.length > 0) {
                    last += tempVector[tempVector.length - 1];
                }
            }

            if (coefficients.size() > floor0.f0_order()) {
                coefficients.resize(floor0.f0_order());
            }

            float[] coeffArr = coefficients.toArray();
            this.floor0Data = new Floor0Data(floor0, false, coeffArr, amplitude);
        } else {
            //amplitude 0 means unused this frame
            this.floor0Data = new Floor0Data(floor0, true, null, 0);
        }
    }

    private void decodeFloor1() throws IOException {
        boolean nonzero = bitStream.readBits(1) == 1;
        if (!nonzero) {
            this.floor1Data = new Floor1Data(true);
            return;
        }

        Floor1 floor1 = (Floor1) floor.floor;

        int[] floor1_Y = new int[floor1.xList().length];

        int multiplier = floor1.multiplier();
        int range = Floor1Data.RANGE_VALS[multiplier - 1];
        int toRead = Helpers.ilog(range - 1);

        floor1_Y[0] = bitStream.readBits(toRead);
        floor1_Y[1] = bitStream.readBits(toRead);

        int offset = 2;

        for (int i = 0; i < floor1.partitions(); i++) {
            int klass = floor1.partitionClassList()[i];
            int cdim = floor1.classDimensions()[klass];
            int cbits = floor1.classSubclasses()[klass];
            int csub = (1 << cbits) - 1;

            int cval = 0;
            if (cbits > 0) {
                int codeBookNr = floor1.classMasterbooks()[klass];
                Codebook codebook = d.setup.codebooks[codeBookNr];
                cval = codebook.getSymbolDecoder().decodeSymbol(bitStream);
            }

            for (int j = 0; j < cdim; j++) {
                int bookIndexInClass = cval & csub;
                int book = floor1.subclassBooks()[klass][bookIndexInClass];
                cval = cval >>> cbits;

                if (book >= 0) {
                    Codebook subBook = d.setup.codebooks[book];
                    int y = subBook.getSymbolDecoder().decodeSymbol(bitStream);
                    floor1_Y[j + offset] = Math.clamp(y, 0, range);
                } else {
                    floor1_Y[j + offset] = 0;
                }
            }

            offset += cdim;
        }

        boolean[] step2Flag = new boolean[floor1.values()];
        step2Flag[0] = true;
        step2Flag[1] = true;

        int[] finalY = new int[floor1_Y.length];
        finalY[0] = floor1_Y[0];
        finalY[1] = floor1_Y[1];

        int[] xList = floor1.xList();

        for (int i = 2; i < floor1.values(); i++) {
            int lowNeighborOff = Helpers.low_neighbor(xList, i);
            int highNeighborOff = Helpers.high_neighbor(xList, i);

            int predicted = Helpers.render_point(
                    xList[lowNeighborOff], finalY[lowNeighborOff],
                    xList[highNeighborOff], finalY[highNeighborOff],
                    xList[i]
            );

            int val = floor1_Y[i];
            int highroom = range - predicted;
            int lowroom = predicted;
            int room = Math.min(highroom, lowroom) * 2;

            if (val != 0) {
                step2Flag[lowNeighborOff] = true;
                step2Flag[highNeighborOff] = true;
                step2Flag[i] = true;

                if (val >= room) {
                    if (highroom > lowroom) {
                        finalY[i] = val - lowroom + predicted;
                    } else {
                        finalY[i] = predicted - val + highroom - 1;
                    }
                } else {
                    if ((val & 1) != 0) {
                        finalY[i] = predicted - ((val + 1) >>> 1);
                    } else {
                        finalY[i] = predicted + (val >>> 1);
                    }
                }
            } else {
                step2Flag[i] = false;
                finalY[i] = predicted;
            }
        }

        this.floor1Data = new Floor1Data(false, xList, finalY, step2Flag, multiplier, floor1.values());
    }
}
