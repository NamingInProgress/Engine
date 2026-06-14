package com.vke.test.audio;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.audio.AudioClip;
import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.utils.io.Identifier;

public class AudioScene extends Scene {
    public AudioScene(Identifier name, Context context) {
        super(name, context);
    }

    @Override
    public void onLoad() {
        try {
            AssetHandle<AudioClip> preloaded = R.audios.get("preloaded.wav");
            AudioClip clip = preloaded.acquire(context);
            System.out.println(clip);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUnload() {

    }

    @Override
    public void free() {

    }
}
