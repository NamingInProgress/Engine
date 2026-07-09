package com.vke.test.audio;

import com.vke.core.audio.PreloadedAudioClip;
import com.vke.core.audio.pcm.PCMInfo;
import com.vke.core.audio.pcm.reader.ArrayPCMReader;
import com.vke.core.audio.source.AudioClip;

public class ToneGenerator {
    public static AudioClip sinTonePreloaded(float durationSeconds, int numChannels) {
        int sampleRate = 48000;
        float frequency = 440.0f;
        int totalFrames = (int) (sampleRate * durationSeconds);

        float[][] pcm = new float[totalFrames][numChannels];

        for (int i = 0; i < totalFrames; i++) {
            float time = (float) i / sampleRate;
            float sample = (float) Math.sin(2.0 * Math.PI * frequency * time);

            for (int c = 0; c < numChannels; c++) {
                pcm[i][c] = sample * 0.2f;
            }
        }
        PCMInfo info = new PCMInfo(sampleRate, numChannels, 16, totalFrames);
        return new PreloadedAudioClip(new ArrayPCMReader(pcm, info));
    }
}
