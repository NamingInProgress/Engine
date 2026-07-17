package com.vke.core.file.ogg.vorbis.data.floor;

import com.carrotsearch.hppc.FloatArrayList;
import com.vke.core.file.io.bit.input.BitInputStream;
import com.vke.core.file.ogg.vorbis.Helpers;
import com.vke.core.file.ogg.vorbis.VorbisStreamUndecodableException;
import com.vke.core.file.ogg.vorbis.header.setup.Codebook;
import com.vke.core.file.ogg.vorbis.header.setup.FloorConfig;
import com.vke.core.file.ogg.vorbis.header.setup.floor.Floor0;
import com.vke.utils.Utils;

import java.io.IOException;

public class FloorCurveData {
    private final BitInputStream bitStream;
    private final FloorCurveDecoder d;
    public final int ch;

    private final int submapNum;
    private final FloorConfig floor;

    private Floor0Data floor0Data;

    public FloorCurveData(FloorCurveDecoder d, int ch) throws IOException {
        this.bitStream = d.bitStream;
        this.d = d;
        this.ch = ch;

        this.submapNum = d.mapping.mux()[ch];
        int floorNum = d.mapping.floor()[submapNum];
        this.floor = d.setup.floorConfigs[floorNum];
        int floorType = floor.type;
        if (floorType == 0) {
            decodeFloor0();
        } else if (floorType == 1) {

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
}
