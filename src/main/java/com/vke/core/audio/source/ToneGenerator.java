package com.vke.core.audio.source;

import com.vke.core.audio.PreloadedAudioClip;
import com.vke.core.audio.pcm.PCMInfo;
import com.vke.core.audio.pcm.reader.ArrayPCMReader;
import com.vke.core.audio.playback.PlaybackState;

public class ToneGenerator {
    public static AudioClip generateTone(long duration, int frequency) {
        int sampleRate = PlaybackState.SAMPLE_RATE;
        int channels = 1;
        int bitsPerSample = 16;
        int totalFrames = (int) (sampleRate * duration / 1000);

        PCMInfo info = new PCMInfo(sampleRate, channels, bitsPerSample, totalFrames);
        float[][] pcm = new float[totalFrames][channels];

        for (int i = 0; i < totalFrames; i++) {
            pcm[i][0] = (float) Math.sin(2.0 * Math.PI * frequency * i / sampleRate);
        }

        return new PreloadedAudioClip(new ArrayPCMReader(pcm, info));
    }
}
