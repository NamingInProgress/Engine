package com.vke.core.file.ogg.vorbis.data;

import com.vke.core.file.io.bit.input.BitInputStream;
import com.vke.core.file.ogg.vorbis.Helpers;
import com.vke.core.file.ogg.vorbis.data.floor.FloorCurveDecoder;
import com.vke.core.file.ogg.vorbis.data.window.WindowData;
import com.vke.core.file.ogg.vorbis.data.window.WindowDecoder;
import com.vke.core.file.ogg.vorbis.data.window.WindowPart;
import com.vke.core.file.ogg.vorbis.header.IdentHeader;
import com.vke.core.file.ogg.vorbis.header.VorbisPCMInfoExtension;
import com.vke.core.file.ogg.vorbis.header.setup.Mode;
import com.vke.core.file.ogg.vorbis.header.setup.SetupHeader;

import java.io.IOException;

public class VorbisAudioPacketDecoder {
    private final VorbisPCMInfoExtension info;
    private final SetupHeader setup;
    private final IdentHeader ident;

    private BitInputStream bitStream;
    private WindowPart prevWindow;

    public VorbisAudioPacketDecoder(VorbisPCMInfoExtension info) {
        this.info = info;
        this.setup = info.setupHeader();
        this.ident = info.identHeader();
    }

    public Object decodeAudioPacket(BitInputStream bitStream) throws IOException {
        this.bitStream = bitStream;

        int modeCount = setup.modes.length;
        int modeNumber = bitStream.readBits(Helpers.ilog(modeCount - 1));
        Mode mode = setup.modes[modeNumber];

        WindowDecoder windowDecoder = new WindowDecoder(info, mode, bitStream);
        WindowData windowData = windowDecoder.decodeWindow();

        FloorCurveDecoder floorCurve = new FloorCurveDecoder(info, bitStream, mode);

    }


}
