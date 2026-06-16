package com.vke.core.assets.pipeline.protocols.audio;

import com.vke.api.assets.Protocols;
import com.vke.core.assets.pipeline.apis.AbstractAssetProtocol;
import com.vke.core.audio.source.AudioClip;

public class AudioPreloadProtocol implements AbstractAssetProtocol<AudioClip> {
    @Override
    public String getProtocolName() {
        return Protocols.AUDIO_PRELOADED;
    }
}
