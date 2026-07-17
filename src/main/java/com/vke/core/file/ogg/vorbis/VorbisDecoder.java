package com.vke.core.file.ogg.vorbis;

import com.vke.core.audio.decoder.AudioDecoder;
import com.vke.core.audio.pcm.PCMInfo;
import com.vke.core.file.io.bit.BitOrdering;
import com.vke.core.file.io.bit.BitStreamUtils;
import com.vke.core.file.io.bit.input.BitInputStream;
import com.vke.core.file.io.bit.input.ShittyBitInputStream;
import com.vke.core.file.ogg.OggPacketReader;
import com.vke.core.file.ogg.vorbis.data.VorbisAudioPacketDecoder;
import com.vke.core.file.ogg.vorbis.header.CommentHeader;
import com.vke.core.file.ogg.vorbis.header.IdentHeader;
import com.vke.core.file.ogg.vorbis.header.VorbisHeaderPacketHeader;
import com.vke.core.file.ogg.vorbis.header.VorbisPCMInfoExtension;
import com.vke.core.file.ogg.vorbis.header.setup.SetupHeader;
import com.vke.core.file.utils.DataUtils;
import com.vke.utils.Utils;
import com.vke.utils.types.Container;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class VorbisDecoder implements AudioDecoder {
    public static final int[] VORBIS_MAGIC = {0x76, 0x6f, 0x72, 0x62, 0x69, 0x73};

    private final OggPacketReader oggDecoder;

    private InputStream stream;
    private BitInputStream bitStream;

    private VorbisPCMInfoExtension info;

    private VorbisAudioPacketDecoder audioPacketDecoder;

    public VorbisDecoder(InputStream stream) {
        this.oggDecoder = new OggPacketReader(stream);
    }


    @Override
    public PCMInfo decodeMeta() throws IOException {
        IdentHeader ident = null;
        CommentHeader comment = null;
        SetupHeader setup = null;

        for (int i = 0; i < 3; i++) {
            VorbisHeaderPacketHeader h = startHeaderPacket();
            if (h.isIdent()) ident = new IdentHeader(bitStream);
            else if (h.isComment()) comment = new CommentHeader(bitStream);
            else if (h.isSetup() && ident != null) setup = new SetupHeader(ident, bitStream);
            else throw new VorbisStreamUndecodableException();
        }

        if (Utils.anyNull(ident, comment, setup)) {
            throw new VorbisStreamUndecodableException();
        }

        //to make intellij happy :)
        assert ident != null && comment != null && setup != null;

        bitStream.alignToByte();

        this.info = new VorbisPCMInfoExtension(ident, comment, setup);
        this.audioPacketDecoder = new VorbisAudioPacketDecoder(info);
        return new PCMInfo((int) ident.sampleRate, ident.channels, 32, -1, info);
    }

    @Override
    public int decodeNextFrames(Container<float[][]> out, int offset) {

        return 0;
    }

    @Override
    public void createState(long framePos) {

    }

    private void setupPacket() throws IOException {
        byte[] packet = oggDecoder.readNextPacket();
        this.stream = new ByteArrayInputStream(packet);
        this.bitStream = new ShittyBitInputStream(stream);
        this.bitStream.setOrdering(BitOrdering.LSB_FIRST);
    }

    private VorbisHeaderPacketHeader startHeaderPacket() throws IOException {
        setupPacket();

        int type = DataUtils.readU8(stream);
        int[] V_O_R_B_I_S = DataUtils.readU8N(6, stream);
        if (!Arrays.equals(V_O_R_B_I_S, VORBIS_MAGIC)) {
            throw new IOException("Not a vorbis packet!");
        }
        return new VorbisHeaderPacketHeader(type);
    }

    private Object decodeAudioPacket() throws IOException {
        setupPacket();

        boolean isntAudioPacket = BitStreamUtils.readFlag(bitStream);
        if (!isntAudioPacket) {
            return audioPacketDecoder.decodeAudioPacket(bitStream);
        }
        //spec says:
        //"the decoder must ignore the packet and not attempt decoding it to audio"
        return decodeAudioPacket();
    }
}
