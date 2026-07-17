package com.vke.test.oggvorbis;

import com.vke.core.file.ogg.OggPacketReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class OggRawTest {
    public static void main(String[] args) throws IOException {
        try(InputStream stream = new FileInputStream("Audio_test_german.ogg")) {
            OggPacketReader packetReader = new OggPacketReader(stream);
            int packets = 0;
            byte[] packet;
            while ((packet = packetReader.readNextPacket()) != null) {
                packets++;
            }
            System.out.println("totoal packets: " + packets);
        }
    }
}
