package com.vke.core.audio.playback3d;

import com.vke.core.audio.playback.Mixer;
import com.vke.core.audio.playback.PlaybackState;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Mixer3D implements Mixer {
    public static final int MAX_SOUNDS = 256;
    public static final float MAX_DISTANCE = 200;

    private final ArrayList<PlaybackState3D> active;
    private final ConcurrentLinkedQueue<PlaybackState3D> queued;

    private final Vector3f directionToSpeaker = new Vector3f();
    private final Vector3f earRightDirection = new Vector3f();

    private Ear ear;

    private final int channels;

    private float volume;

    public Mixer3D(int channels) {
        this(MAX_SOUNDS, channels);
    }

    public Mixer3D(int maxSounds, int channels) {
        this.active = new ArrayList<>(maxSounds);
        this.channels = channels;
        this.queued = new ConcurrentLinkedQueue<>();
        this.volume = 1f;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void request(PlaybackState3D playbackState) {
        queued.add(playbackState);
    }

    public int getChannels() {
        return channels;
    }

    @Override
    public void newBlock() {
        PlaybackState3D v;
        while ((v = queued.poll()) != null) {
            active.add(v);
        }

        if (ear == null) return;

        for (PlaybackState3D state3D : active) {
            Vector3f earPos = ear.getPosition();
            Vector3f speakerPos = state3D.getSpeaker().getPosition();
            Quaternionf earRot = ear.getRotation();
            earRot.transform(1f, 0f, 0f, earRightDirection);

            float actualDistance = earPos.distance(speakerPos);

            if (actualDistance > MAX_DISTANCE) {
                state3D.setTargetGains(0, 0);
            } else {
                float distanceVolume = 1f - (actualDistance / MAX_DISTANCE);
                float pan = 0f;

                if (actualDistance > 0.001f) {
                    speakerPos.sub(earPos, directionToSpeaker);
                    directionToSpeaker.normalize();

                    pan = directionToSpeaker.dot(earRightDirection);
                }

                float leftPanGain  = (pan <= 0) ? 1f : 1f - pan;
                float rightPanGain = (pan >= 0) ? 1f : 1f + pan;

                float finalLeft  = distanceVolume * leftPanGain  * state3D.getSpeaker().getVolume();
                float finalRight = distanceVolume * rightPanGain * state3D.getSpeaker().getVolume();

                state3D.setTargetGains(finalLeft, finalRight);
            }
        }
    }

    @Override
    public void mixNextFrame(float[] out) {
        ListIterator<PlaybackState3D> it = active.listIterator();
        int blockSize = PlaybackState.BLOCK_SIZE;

        while (it.hasNext()) {
            PlaybackState3D state3d = it.next();
            PlaybackState state = state3d.getInnerState();

            if (!state.hasMoreFrames()) {
                it.remove();
                continue;
            }

            float trackGain = volume * state.getVolume();

            float targetL = state3d.getTargetLeftGain() * trackGain;
            float targetR = state3d.getTargetRightGain() * trackGain;

            float currentL = state3d.getCurrentLeftGain();
            float currentR = state3d.getCurrentRightGain();

            float leftStep  = (targetL - currentL) / blockSize;
            float rightStep = (targetR - currentR) / blockSize;

            int framesToMix = out.length / 2;

            for (int f = 0; f < framesToMix; f++) {
                if (!state.hasMoreFrames()) {
                    break;
                }

                float[] s = state.nextFrame();

                currentL += leftStep;
                currentR += rightStep;

                int baseIdx = f * 2;
                if (s.length == 1) {
                    out[baseIdx]     += s[0] * currentL;
                    out[baseIdx + 1] += s[0] * currentR;
                } else {
                    out[baseIdx]     += s[0] * currentL;
                    out[baseIdx + 1] += s[1] * currentR;
                }
            }

            state3d.setCurrentGains(currentL, currentR);
        }
    }

    public float getVolume() {
        return volume;
    }

    public void setEar(Ear ear) {
        this.ear = ear;
    }
}
