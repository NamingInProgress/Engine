package com.vke.core.assets.pipeline.protocols.audio;

import com.vke.api.assets.Protocols;
import com.vke.api.assets.anot.Protocol;
import com.vke.core.assets.pipeline.apis.AbstractAssetProtocol;
import com.vke.core.file.wav.WAVFile;

@Protocol
public class WavPreloadProtocol implements AbstractAssetProtocol<WAVFile> {
    @Override
    public String getProtocolName() {
        return Protocols.WAV;
    }
}
