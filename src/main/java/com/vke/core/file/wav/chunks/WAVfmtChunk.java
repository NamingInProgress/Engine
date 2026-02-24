package com.vke.core.file.wav.chunks;

import com.vke.core.file.riff.RIFFChunk;
import com.vke.core.file.riff.RIFFChunkFactory;
import com.vke.core.file.riff.RIFFFormat;
import com.vke.core.file.riff.RIFFPayload;
import com.vke.core.file.utils.Ascii4;
import com.vke.core.file.utils.DataUtils;
import com.vke.core.file.wav.WAVAudioFormat;
import com.vke.core.file.wav.WAVExtensible;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class WAVfmtChunk extends RIFFChunk {
    public static final Ascii4 NAME = Ascii4.of("fmt ");

    public WAVfmtChunk(Ascii4 name, InputStream stream, RIFFFormat format) throws IOException {
        super(name, stream, format);
    }

    @Override
    protected RIFFPayload readPayload(InputStream stream, RIFFFormat format) throws IOException {
        return new Payload(stream, size);
    }

    public static class Factory implements RIFFChunkFactory {

        @Override
        public RIFFChunk readChunk(Ascii4 name, InputStream stream, RIFFFormat format) throws IOException {
            return new WAVfmtChunk(name, stream, format);
        }
    }

    public static class Payload extends RIFFPayload {
        private final WAVAudioFormat audioFormat;
        private final int numChannels;
        private final long sampleRate;
        private final long byteRate;
        private final int blockAlign;
        private final int bitsPerSample;

        //extended format
        private final WAVExtensible extensible;

        public Payload(InputStream stream, long chunkSize) throws IOException {
            int rawFormat = DataUtils.readU16LittleEndian(stream);
            this.audioFormat = WAVAudioFormat.fromCode(rawFormat);
            this.numChannels = DataUtils.readU16LittleEndian(stream);
            this.sampleRate = DataUtils.unsign32(DataUtils.readU32LittleEndian(stream));
            this.byteRate = DataUtils.unsign32(DataUtils.readU32LittleEndian(stream));
            this.blockAlign = DataUtils.readU16LittleEndian(stream);
            this.bitsPerSample = DataUtils.readU16LittleEndian(stream);

            long remaining = chunkSize - 16;
            if (remaining >= 2) {
                int cbSize = DataUtils.readU16LittleEndian(stream);
                remaining -= 2;

                if (rawFormat == 0xFFFE && cbSize >= 22 && remaining >= 22) {
                    int validBits = DataUtils.readU16LittleEndian(stream);
                    long channelMask = DataUtils.readU32LittleEndian(stream);
                    UUID subFormat = DataUtils.readGuidLittleEndian(stream);

                    this.extensible = new WAVExtensible(validBits, channelMask, subFormat);
                    remaining -= 22;
                } else {
                    this.extensible = null;
                }

                if (remaining > 0) {
                    stream.skipNBytes(remaining);
                }
            } else {
                this.extensible = null;
            }
        }

        public WAVAudioFormat getAudioFormat() {
            return audioFormat;
        }

        public int getNumChannels() {
            return numChannels;
        }

        public long getSampleRate() {
            return sampleRate;
        }

        public long getByteRate() {
            return byteRate;
        }

        public int getBlockAlign() {
            return blockAlign;
        }

        public int getBitsPerSample() {
            return bitsPerSample;
        }

        public WAVExtensible getExtensible() {
            return extensible;
        }

        @Override
        public String toString() {
            return "Payload{" +
                    "audioFormat=" + audioFormat +
                    ", numChannels=" + numChannels +
                    ", sampleRate=" + sampleRate +
                    ", byteRate=" + byteRate +
                    ", blockAlign=" + blockAlign +
                    ", bitsPerSample=" + bitsPerSample +
                    ", extensible=" + extensible +
                    '}';
        }
    }
}
