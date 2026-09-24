package com.vke.core.audio.playback3d;

import com.vke.core.audio.playback.Mixer;
import com.vke.core.audio.playback.PlaybackState;
import com.vke.core.game.object.GameObjectTransform;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Mixer3D implements Mixer {
    public static final int MAX_SOUNDS = 256;
    public static final float MAX_DISTANCE = 200;
    public static final int GAIN_RAMP_FRAMES = PlaybackState.SAMPLE_RATE / 50;

    private final ArrayList<PlaybackState3D> active;
    private final ConcurrentLinkedQueue<PlaybackState3D> queued;

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

    private static final Vector3f earPos = new Vector3f();
    private static final Vector3f speakerPos = new Vector3f();
    private static final Vector4f earForward = new Vector4f();
    private static final Vector4f speakerForward = new Vector4f();

    private static final Vector3f earRightDirection = new Vector3f();
    private static final Vector3f directionToSpeaker = new Vector3f();
    private static final Vector3f WORLD_UP = new Vector3f(0f, 1f, 0f);

    @Override
    public synchronized void newBlock() {
        PlaybackState3D v;
        while ((v = queued.poll()) != null) {
            active.add(v);
        }

        if (ear == null) return;

        GameObjectTransform earTransform = ear.getTransform();
        earTransform.getWorldPosition(earPos);
        earTransform.getWorldForward(earForward);

        earRightDirection.set(earForward.x, earForward.y, earForward.z)
                .cross(WORLD_UP);

        if (earRightDirection.lengthSquared() < 0.0001f) {
            earRightDirection.set(1f, 0f, 0f);
        } else {
            earRightDirection.normalize();
        }

        for (PlaybackState3D state3D : active) {
            GameObjectTransform speakerTransform = state3D.getSpeaker().getTransform();

            speakerTransform.getWorldPosition(speakerPos);
            speakerTransform.getWorldForward(speakerForward);

            float actualDistance = earPos.distance(speakerPos);

            if (actualDistance > MAX_DISTANCE) {
                state3D.setTargetGains(0f, 0f);
            } else {
                float distanceVolume = 1f - (actualDistance / MAX_DISTANCE);
                float pan = 0f;

                if (actualDistance > 0.001f) {
                    speakerPos.sub(earPos, directionToSpeaker);
                    directionToSpeaker.normalize();
                    pan = directionToSpeaker.dot(earRightDirection);
                }

                float leftPanGain  = (pan <= 0f) ? 1f : 1f - pan;
                float rightPanGain = (pan >= 0f) ? 1f : 1f + pan;

                float speakerVolume = state3D.getSpeaker().getVolume();
                float finalLeft  = distanceVolume * leftPanGain  * speakerVolume;
                float finalRight = distanceVolume * rightPanGain * speakerVolume;

                state3D.setTargetGains(finalLeft, finalRight);
            }
        }
    }

    @Override
    public void mixNextFrame(float[] out) {
        ListIterator<PlaybackState3D> it = active.listIterator();

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

            float leftStep = (targetL - currentL) / GAIN_RAMP_FRAMES;
            float rightStep = (targetR - currentR) / GAIN_RAMP_FRAMES;

            currentL += leftStep;
            currentR += rightStep;

            float[] s = state.nextFrame();

            if (s.length == 1) {
                out[0] += s[0] * currentL;
                out[1] += s[0] * currentR;
            } else {
                out[0] += s[0] * currentL;
                out[1] += s[1] * currentR;
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
