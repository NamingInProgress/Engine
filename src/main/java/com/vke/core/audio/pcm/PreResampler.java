package com.vke.core.audio.pcm;

public class PreResampler {
    private final int sourceRate, targetRate;
    private final float[][] inputPcm;
    private final PCMInfo info;

    public PreResampler(int sourceRate, int targetRate, float[][] inputPcm, PCMInfo info) {
        this.sourceRate = sourceRate;
        this.targetRate = targetRate;
        this.inputPcm = inputPcm;
        this.info = info;
    }

    public float[][] resample() {
        if (sourceRate == targetRate) {
            return inputPcm;
        } else {
            int numChannels = info.channels;
            int totalFrames = inputPcm.length;

            double ratio = (double) targetRate / sourceRate;
            int targetTotalFrames = (int) Math.round(totalFrames * ratio);
            float[][] pcm = new float[targetTotalFrames][numChannels];

            for (int e = 0; e < targetTotalFrames; e++) {
                double srcFrameIndex = e / ratio;
                int lowFrame = (int) Math.floor(srcFrameIndex);
                int highFrame = Math.min(lowFrame + 1, totalFrames - 1);
                float t = (float) (srcFrameIndex - lowFrame);

                for (int c = 0; c < numChannels; c++) {
                    float sampleLow = inputPcm[lowFrame][c];
                    float sampleHigh = inputPcm[highFrame][c];

                    pcm[e][c] = sampleLow + t * (sampleHigh - sampleLow);
                }
            }

            return pcm;
        }
    }
}
