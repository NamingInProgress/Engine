package com.vke.core.assets.pipeline.protocols.audio;

import com.vke.api.assets.Protocols;
import com.vke.api.audio.AudioClip;
import com.vke.core.assets.pipeline.apis.AbstractAssetProtocol;

public class AudioPreloadProtocol implements AbstractAssetProtocol<AudioClip> {
    @Override
    public String getProtocolName() {
        return Protocols.AUDIO_PRELOADED;
    }
}
