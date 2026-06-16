package com.vke.core.audio.source;

import com.vke.core.audio.AudioException;
import com.vke.core.audio.playback.PlaybackState;
import com.vke.core.audio.pcm.PCMInfo;
import com.vke.core.audio.pcm.reader.PCMReader;
import com.vke.core.audio.pcm.PreResampler;
import com.vke.core.file.wav.WAVAudioFormat;
import com.vke.core.file.wav.WAVFile;
import com.vke.core.file.wav.chunks.WAVfmtChunk;
import com.vke.utils.exception.Unreachable;

public class WavPCMPreloadedReader implements PCMReader {
    private final PCMInfo info;
    private final WAVfmtChunk.Payload fmt;
    private long position;
    private final float[][] pcm;

    public WavPCMPreloadedReader(WAVFile wavFile) throws AudioException {
        this.fmt = wavFile.getFmtPayload();
        switch (fmt.getBitsPerSample()) {
            case 8, 16, 24, 32 -> {}
            default -> throw new AudioException("Unsupported bit depth: " + fmt.getBitsPerSample());
        }

        //how fucking high is your sample rate that this conversion throws damn
        this.info = new PCMInfo((int) fmt.getSampleRate(), fmt.getNumChannels(), fmt.getBitsPerSample());

        byte[] sampleData = wavFile.getRawSampleData();

        int sampleRate = (int) fmt.getSampleRate();
        int targetSampleRate = PlaybackState.SAMPLE_RATE;
        int bytesPerFrame = fmt.getBlockAlign();
        int bytesPerSample = fmt.getBitsPerSample() / 8;
        int totalFrames = sampleData.length / bytesPerFrame;
        int numChannels = fmt.getNumChannels();

        float[][] originalPcm = new float[totalFrames][numChannels];
        for (int i = 0, p = 0; i < sampleData.length;) {
            float[] channels = originalPcm[p++];
            for (int c = 0; c < channels.length; c++) {
                channels[c] = readSample(i, sampleData);
                i += bytesPerSample;
            }
        }

        if (sampleRate == targetSampleRate) {
            this.pcm = originalPcm;
        } else {
            PreResampler resampler = new PreResampler(sampleRate, targetSampleRate, originalPcm, info);
            this.pcm = resampler.resample();
        }
    }

    private float readSample(int byteOffset, byte[] sampleData) throws AudioException {
        float normalizer;
        int bits = switch(fmt.getBitsPerSample()) {
            case 8 -> {
                int s = sampleData[byteOffset] & 0xFF;
                normalizer = 128f;
                yield s - 128;
            }
            case 16 -> {
                int a = sampleData[byteOffset] & 0xFF;
                int b = sampleData[byteOffset + 1] & 0xFF;
                int s = a | (b << 8);
                normalizer = 32768f;
                yield (short) s; //remove sign
            }
            case 24 -> {
                int a = sampleData[byteOffset] & 0xFF;
                int b = sampleData[byteOffset + 1] & 0xFF;
                int c = sampleData[byteOffset + 2] & 0xFF;
                int s = a | (b << 8) | (c << 16);
                //yet another sign fix...
                if ((s & 0x800000) != 0) {
                    s |= 0xFF000000;
                }
                normalizer = 8388608f;
                yield s;
            }
            case 32 -> {
                int a = sampleData[byteOffset] & 0xFF;
                int b = sampleData[byteOffset + 1] & 0xFF;
                int c = sampleData[byteOffset + 2] & 0xFF;
                int d = sampleData[byteOffset + 3] & 0xFF;
                normalizer = 2147483648f;
                yield (a) | ((b) << 8) | ((c) << 16) | ((d) << 24);
            }
            default -> throw new Unreachable("Case covered earlier");
        };
        WAVAudioFormat audioFormat = fmt.getAudioFormat();
        return switch (audioFormat) {
            case PCM -> bits / normalizer;
            case IEEE_FLOAT -> Float.intBitsToFloat(bits);
            case ALAW, MULAW -> {
                throw new AudioException("Audio format " + audioFormat + " is so old that only old ass telephones use it appearently. i did NOT see the need to implement ts sorry bro :)");
            }
            default -> {
                throw new AudioException("Audio format " + audioFormat + " isnt supported at the moment!");
            }
        };
    }

    @Override
    public int fetchFrames(float[][] dst, int dstPos, int frames) {
        int toCopy = (int) Math.min(frames, pcm.length - position);
        System.arraycopy(pcm, (int) position, dst, dstPos, toCopy);
        return toCopy;
    }

    @Override
    public void seek(long frame) {
        position = frame;
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public PCMInfo getInfo() {
        return info;
    }
}
