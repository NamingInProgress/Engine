package com.vke.core.assets.pipeline.converters.audio.preload;

import com.vke.api.assets.Protocols;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.core.Context;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetConverter;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.audio.PreloadedAudioClip;
import com.vke.core.audio.pcm.reader.PCMReader;
import com.vke.core.audio.source.WavPCMPreloadedReader;
import com.vke.core.file.wav.WAVFile;
import com.vke.utils.Utils;

public class WavAudioPreloadConverter implements AssetConverter {
    @Override
    public String from() {
        return Protocols.WAV;
    }

    @Override
    public String to() {
        return Protocols.AUDIO_PRELOADED;
    }

    @Override
    public AssetData performConversion(Context context, StageElement input, ConfigArrayNode arguments) throws AssetException {
        return Utils.chainExceptions(() -> {
            WAVFile file = input.getAssetData().getDataAs();
            PCMReader reader = new WavPCMPreloadedReader(file);

            return new AssetData(to(), new PreloadedAudioClip(reader));
        });
    }
}
