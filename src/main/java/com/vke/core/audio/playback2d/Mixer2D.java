package com.vke.core.audio.playback2d;

import com.vke.core.audio.playback.Mixer;
import com.vke.core.audio.playback.PlaybackState;
import com.vke.utils.Utils;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Mixer2D implements Mixer {
    public static final int MAX_SOUNDS = 256;

    private final ArrayList<PlaybackState> active;
    private final ConcurrentLinkedQueue<PlaybackState> queued;

    private final int channels;

    private float volume;

    public Mixer2D(int channels) {
        this(MAX_SOUNDS, channels);
    }

    public Mixer2D(int maxSounds, int channels) {
        this.active = new ArrayList<>(maxSounds);
        this.channels = channels;
        this.queued = new ConcurrentLinkedQueue<>();
        this.volume = 1f;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void request(PlaybackState playbackState) {
        queued.add(playbackState);
    }

    public int getChannels() {
        return channels;
    }

    @Override
    public void newBlock() {
        PlaybackState v;
        while ((v = queued.poll()) != null) {
            active.add(v);
        }
    }

    @Override
    public void mixNextFrame(float[] out) {
        ListIterator<PlaybackState> it = active.listIterator();

        while (it.hasNext()) {
            PlaybackState state = it.next();

            if (!state.hasMoreFrames()) {
                it.remove();
                break;
            }

            float[] s = state.nextFrame();

            float gain = volume * state.getVolume();
            float pan = state.getPan();

            float leftGain = (pan <= 0) ? 1f : 1f - pan;
            float rightGain = (pan >= 0) ? 1f : 1f + pan;

            if (channels == 1 && out.length >= 1) {
                float mono = s[0] * gain;
                out[0] += mono;
            } else if (s.length == 1 && channels == 2 && out.length >= 2) {
                float mono = s[0] * gain;
                out[0] += mono * leftGain;
                out[1] += mono * rightGain;
            } else {
                int lim = Utils.xmin(channels, s.length, out.length);
                for (int c = 0; c < lim; c++) {
                    float panGain = (c == 0) ? leftGain : rightGain;
                    out[c] += s[c] * gain * panGain;
                }
            }
        }
    }

    public float getVolume() {
        return volume;
    }
}
