package com.vke.core.file.ogg.vorbis.header;

public record VorbisHeaderPacketHeader(int packetType) {
    public boolean isIdent() {
        return packetType == 1;
    }

    public boolean isComment() {
        return packetType == 3;
    }

    public boolean isSetup() {
        return packetType == 5;
    }
}
