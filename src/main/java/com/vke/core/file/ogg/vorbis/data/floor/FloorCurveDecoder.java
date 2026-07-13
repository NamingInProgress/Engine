package com.vke.core.file.ogg.vorbis.data.floor;

import com.vke.core.file.io.bit.input.BitInputStream;
import com.vke.core.file.ogg.vorbis.header.IdentHeader;
import com.vke.core.file.ogg.vorbis.header.VorbisPCMInfoExtension;
import com.vke.core.file.ogg.vorbis.header.setup.Mapping;
import com.vke.core.file.ogg.vorbis.header.setup.Mode;
import com.vke.core.file.ogg.vorbis.header.setup.SetupHeader;

import java.io.IOException;

public class FloorCurveDecoder {
    final BitInputStream bitStream;
    final Mode mode;
    final Mapping mapping;
    final IdentHeader ident;
    final SetupHeader setup;

    public FloorCurveDecoder(VorbisPCMInfoExtension info, BitInputStream bitStream, Mode mode) throws IOException {
        this.bitStream = bitStream;
        this.mode = mode;
        this.ident = info.identHeader();
        this.setup = info.setupHeader();
        this.mapping = setup.mappings[mode.mapping()];
    }

    public FloorCurveData[] decodeCurves() throws IOException {
        FloorCurveData[] curves = new FloorCurveData[ident.channels];
        for (int i = 0; i < ident.channels; i++) {
            curves[i] = new FloorCurveData(this, i);
        }
        return curves;
    }
}
