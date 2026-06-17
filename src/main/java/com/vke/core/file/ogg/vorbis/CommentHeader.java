package com.vke.core.file.ogg.vorbis;

import com.vke.core.file.io.bit.BitStreamUtils;
import com.vke.core.file.io.bit.input.BitInputStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CommentHeader {
    public final String vendorString;
    public final List<String> comments;
    public final boolean framingFlag;

    public CommentHeader(BitInputStream bitStream) throws IOException {
        long vendorLength = BitStreamUtils.readLittleEndian32(bitStream);
        this.vendorString = readUtf8String(bitStream, (int) vendorLength);

        long numComments = BitStreamUtils.readLittleEndian32(bitStream);
        this.comments = new ArrayList<>((int) numComments);

        for (int i = 0; i < numComments; i++) {
            long commentLength = BitStreamUtils.readLittleEndian32(bitStream);
            String comment = readUtf8String(bitStream, (int) commentLength);
            this.comments.add(comment);
        }

        this.framingFlag = (bitStream.readBits(1) & 1) == 1;
        if (!framingFlag) {
            throw new VorbisStreamUndecodableException();
        }

        bitStream.alignToByte();
    }

    private String readUtf8String(BitInputStream bitStream, int n) throws IOException {
        byte[] bytes = new byte[n];
        for (int i = 0; i < n; i++) {
            bytes[i] = (byte) BitStreamUtils.read8(bitStream);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return "CommentHeader{" +
                "vendorString='" + vendorString + '\'' +
                ", comments=" + comments +
                ", framingFlag=" + framingFlag +
                '}';
    }
}
