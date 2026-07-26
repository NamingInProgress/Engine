package com.vke.core.assets.pipeline.converters;

import com.vke.api.assets.Protocols;
import com.vke.api.assets.anot.Converter;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.core.Context;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetConverter;
import com.vke.core.assets.pipeline.apis.AssetData;

import java.nio.file.Paths;

@Converter
public class PlainPathConverter implements AssetConverter {
    @Override
    public String from() {
        return Protocols.PLAIN;
    }

    @Override
    public String to() {
        return Protocols.PATH;
    }

    @Override
    public AssetData performConversion(Context context, StageElement input, ConfigArrayNode arguments) throws AssetException {
        return AssetData.path(Paths.get(input.getAssetData().getDataAs()));
    }
}
