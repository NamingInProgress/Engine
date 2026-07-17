package com.vke.core.audio.playback3d;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.audio.playback.PlayingAudio;
import com.vke.core.Context;
import com.vke.core.audio.pcm.reader.PCMReader;
import com.vke.core.audio.playback.PlaybackState;
import com.vke.core.audio.source.AudioClip;
import com.vke.utils.io.Identifier;
import com.vke.utils.types.AbstractPositionable;

import java.io.IOException;
import java.util.List;

public class VkeSpeaker extends AbstractPositionable implements Speaker {
    private final Context context;
    private final Mixer3D mixer;

    private float gain;

    public VkeSpeaker(Context context, Mixer3D mixer) {
        this.context = context;
        this.mixer = mixer;
        this.gain = 1f;
    }

    @Override
    public PlayingAudio play(AudioClip audio) {
        PCMReader pcm = audio.createReader();
        PlaybackState state = PlaybackState.fromReader(pcm);
        PlaybackState3D state3D = new PlaybackState3D(state, this);
        mixer.request(state3D);
        return state3D;
    }

    @Override
    public PlayingAudio play(AssetHandle<AudioClip> audio) {
        try {
            return play(audio.acquire(context));
        } catch (IOException e) {
            context.getLogger().error("When playing audio " + audio.getMeta().getAssetName(), e);
            return null;
        }
    }

    @Override
    public PlayingAudio play(Identifier audio) {
        return play(R.audios.get(audio));
    }

    @Override
    public PlayingAudio play(String audio) {
        return play(context.id(audio));
    }

    @Override
    public void setVolume(float volume) {
        this.gain = volume;
    }

    @Override
    public float getVolume() {
        return gain;
    }

    @Override
    public String getId() {
        return "SPEAKER";
    }

    @Override
    public List<String> dependencies() {
        return List.of();
    }

    @Override
    public void free() {

    }
}
