package com.vke.impl.ecs.audio;

import com.vke.api.assets.AssetHandle;
import com.vke.api.audio.playback.PlayingAudio;
import com.vke.core.Identifier;
import com.vke.core.audio.playback3d.Speaker;
import com.vke.core.audio.playback3d.service.AudioManager3D;
import com.vke.core.audio.source.AudioClip;
import com.vke.core.ecs.component.Component;
import pl.epsi.EcsComponent;

@EcsComponent
public class SpeakerC implements Component {
    public Speaker[] speaker;
    public PlayingAudio[] playing;

    @Override
    public void initialize(int i) {
        playing[i] = null;
    }

    public void createSpeaker(int i, AudioManager3D audioManager3D) {
        speaker[i] = audioManager3D.createSpeaker();
    }

    public void play(int i, String audio) {
        playing[i] = speaker[i].play(audio);
    }

    public void play(int i, Identifier audio) {
        playing[i] = speaker[i].play(audio);
    }

    public void play(int i, AssetHandle<AudioClip> audio) {
        playing[i] = speaker[i].play(audio);
    }

    public void play(int i, AudioClip audio) {
        playing[i] = speaker[i].play(audio);
    }

    public PlayingAudio getPlaying(int i) {
        return playing[i];
    }
}
