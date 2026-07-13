package com.vke.test.oggvorbis;

import com.vke.core.audio.pcm.PCMInfo;
import com.vke.core.file.ogg.vorbis.VorbisDecoder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class VorbisHeaderTest {
    public static void main(String[] args) throws IOException {
        try(InputStream stream = new FileInputStream("Audio_test_german.ogg")) {
            VorbisDecoder decoder = new VorbisDecoder(stream);
            PCMInfo info = decoder.decodeMeta();
            decoder.decodeAudioPacket();
            System.out.println(info);
        }
    }
}
