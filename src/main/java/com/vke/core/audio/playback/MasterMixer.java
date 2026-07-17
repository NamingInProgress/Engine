package com.vke.core.audio.playback;

import java.util.ArrayList;
import java.util.Arrays;

public class MasterMixer {
    public static final int CHANNELS = 2;
    private final ArrayList<Mixer> mixers;
    private final Object lock;
    private float[] blockCache;
    private float[] mixerCache;

    public MasterMixer() {
        this.mixers = new ArrayList<>(2);
        this.lock = new Object();
    }

    public void addMixer(Mixer mixer) {
        synchronized (lock) {
            this.mixers.add(mixer);
        }
    }

    public void removeMixer(Mixer mixer) {
        synchronized (lock) {
            this.mixers.remove(mixer);
        }
    }

    public float[] mixBlock() {
        int blocksize = PlaybackState.BLOCK_SIZE;

        if (blockCache == null) {
            blockCache = new float[CHANNELS * blocksize];
        }

        if (mixerCache == null) {
            mixerCache = new float[CHANNELS];
        }

        synchronized (lock) {
            for (Mixer mixer : mixers) {
                mixer.newBlock();
            }

            for (int i = 0; i < blocksize; i++) {
                float l = 0;
                float r = 0;
                for (Mixer mixer : mixers) {
                    Arrays.fill(mixerCache, 0);
                    mixer.mixNextFrame(mixerCache);
                    l += mixerCache[0];
                    r += mixerCache[1];
                }
                blockCache[i * 2] = l;
                blockCache[i * 2 + 1] = r;
            }
        }

        return blockCache;
    }
}
