package com.vke.impl.gameobject.convenientapi;

import com.vke.api.assets.AssetHandle;
import com.vke.api.audio.playback.PlayingAudio;
import com.vke.core.Identifier;
import com.vke.core.audio.playback3d.Ear;
import com.vke.core.audio.playback3d.Speaker;
import com.vke.core.audio.playback3d.service.AudioManager3D;
import com.vke.core.audio.source.AudioClip;
import com.vke.core.game.object.GameObject;
import com.vke.core.game.object.GameObjectTransform;
import com.vke.core.services2.Services;

import java.util.IdentityHashMap;

public class GameAudio {
    private static Ear ear;
    private static final IdentityHashMap<GameObject, Speaker> speakers = new IdentityHashMap<>();

    private static AudioManager3D getManager(GameObject o) {
        AudioManager3D m = o.getContext().service(Services.AUDIO_MANAGER_3D);
        return m;
    }

    private static synchronized Speaker getSpeaker(GameObject object, AudioManager3D manager3D) {
        Speaker speaker = speakers.get(object);
        if (speaker == null) {
            speaker = manager3D.createSpeaker(object.getContext());
            speakers.put(object, speaker);
        }
        return speaker;
    }

    public static synchronized void setEar(GameObject earOwner) {
        GameObjectTransform transform = earOwner.getTransform();
        AudioManager3D manager3d = getManager(earOwner);
        if (ear == null) {
            ear = manager3d.createEar();
            manager3d.setListeningEar(ear);
        }

        ear.setTransform(transform);
    }

    private static synchronized Speaker setupSpeaker(GameObject target) {
        AudioManager3D manager3d = getManager(target);
        Speaker speaker = getSpeaker(target, manager3d);
        speaker.setTransform(target.getTransform());
        return speaker;
    }

    public static synchronized PlayingAudio playAudio(GameObject target, AudioClip audio) {
        Speaker speaker = setupSpeaker(target);
        return speaker.play(audio);
    }

    public static synchronized PlayingAudio playAudio(GameObject target, AssetHandle<AudioClip> audio) {
        Speaker speaker = setupSpeaker(target);
        return speaker.play(audio);
    }

    public static synchronized PlayingAudio playAudio(GameObject target, Identifier audio) {
        Speaker speaker = setupSpeaker(target);
        return speaker.play(audio);
    }

    public static synchronized PlayingAudio playAudio(GameObject target, String audio) {
        Speaker speaker = setupSpeaker(target);
        return speaker.play(audio);
    }
}
