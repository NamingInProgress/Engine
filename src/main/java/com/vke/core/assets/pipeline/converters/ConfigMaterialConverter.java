package com.vke.core.assets.pipeline.converters;

import com.vke.api.assets.Protocols;
import com.vke.api.assets.anot.Converter;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.core.Context;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetConverter;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.rendering.pbr.deserialize.MaterialDeserializers;

@Converter
public class ConfigMaterialConverter implements AssetConverter {
    @Override
    public String from() {
        return Protocols.CONFIG;
    }

    @Override
    public String to() {
        return Protocols.MATERIAL;
    }

    @Override
    public AssetData performConversion(Context context, StageElement input, ConfigArrayNode arguments) throws AssetException {
        ConfigDocument doc = input.getAssetData().getDataAs();
        return new AssetData(Protocols.MATERIAL, MaterialDeserializers.parse(context, input.getAssetName(), doc));
    }
}
