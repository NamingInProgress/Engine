package com.vke.core.audio.device;

import com.vke.core.audio.AudioException;
import com.vke.core.audio.playback.PlaybackState;

import javax.sound.sampled.*;

public class JavaAudioDevice implements AudioDevice {
    private final AudioFormat format;
    private final SourceDataLine line;
    private byte[] array;

    public JavaAudioDevice(int channels) throws AudioException {
        this.array = new byte[2 * channels * PlaybackState.BLOCK_SIZE];

        this.format = new AudioFormat(
             AudioFormat.Encoding.PCM_SIGNED,
                PlaybackState.SAMPLE_RATE,
                16,
                channels,
                channels * 2,
                PlaybackState.SAMPLE_RATE,
                false

        );

        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            this.line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
        } catch (LineUnavailableException e) {
            throw new AudioException(e);
        }
    }

    @Override
    //interleaved frames in the format [c1,c2,c3;c1,c2,c3...]
    public int read(float[] interleavedPCM, int frameAmount) {
        int samples = frameAmount * format.getChannels();
        int neededArraySpace = 2 * samples;
        if (neededArraySpace > array.length) {
            //we can safely throw away the contents of array here
            array = new byte[neededArraySpace];
        }

        int arrayIdx = 0;
        for (int i = 0; i < samples; i++) {
            float f = interleavedPCM[i];
            short s = (short)(clamp(f) * 32767);
            array[arrayIdx++] = (byte)(s & 0xFF);
            array[arrayIdx++] = (byte)(s >> 8);
        }
        int actuallyWritten = line.write(array, 0, arrayIdx);
        return actuallyWritten / 2 / format.getChannels();
    }

    private float clamp(float f) {
        return Math.clamp(f, -1, 1);
    }

    @Override
    public void free() {
        this.line.close();
    }
}
