package com.vke.core.assets.pipeline.protocols.audio;

import com.vke.api.assets.Protocols;
import com.vke.core.assets.pipeline.apis.AbstractAssetProtocol;
import com.vke.core.file.wav.WAVFile;

public class WavPreloadProtocol implements AbstractAssetProtocol<WAVFile> {
    @Override
    public String getProtocolName() {
        return Protocols.WAV;
    }
}
