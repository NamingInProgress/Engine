package com.vke.test.audio;

import com.vke.api.audio.playback.PlayingAudio;
import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.audio.playback3d.Ear;
import com.vke.core.audio.playback3d.Speaker;
import com.vke.core.audio.playback3d.service.AudioManager3D;
import com.vke.core.audio.source.AudioClip;
import com.vke.core.services2.Services;
import com.vke.utils.io.Identifier;
import org.joml.Vector3f;

public class AudioScene extends Scene {
    private PlayingAudio playing;
    private Ear ear;

    public AudioScene(Identifier name, Context context) {
        super(name, context);
    }

    @Override
    public void onLoad() throws Exception {
        AudioManager3D audioManager3d = context.service(Services.AUDIO_MANAGER_3D);
        ear = audioManager3d.createEar();
        ear.setPosition(new Vector3f(0, 0, 0));
        audioManager3d.setListeningEar(ear);

        AudioClip tone = ToneGenerator.sinTonePreloaded(1, 2);

        Speaker speaker = audioManager3d.createSpeaker();
        playing = speaker.play(tone);
        playing.setLooping(true);

        Speaker speaker2 = audioManager3d.createSpeaker();
        speaker2.setPosition(new Vector3f(100, 0, 0));
        PlayingAudio playing2 = speaker2.play("preloaded.wav");
        playing2.setLooping(true);
    }

    @Override
    public void preFrame() {
        double timeSeconds = System.nanoTime() * 1e-8;
        float speed = 1.0f;
        float angle = (float) (timeSeconds * speed);

        float radius = 100.0f;

        float newX = radius * (float) Math.cos(angle);
        float newZ = radius * (float) Math.sin(angle);

        ear.setPosition(new Vector3f(newX, 0.0f, newZ));
    }

    @Override
    public void onUnload() throws Exception {

    }

    @Override
    public void free() {

    }
}
