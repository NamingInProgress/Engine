package com.vke.core.assets.pipeline.converters;

import com.vke.api.assets.Protocols;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.core.assets.pipeline.AssetPipelineException;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetConverter;
import com.vke.core.assets.pipeline.apis.AssetData;

public class IdentityConverter implements AssetConverter {
    @Override
    public String from() {
        return Protocols.ANY;
    }

    @Override
    public String to() {
        return Protocols.ANY;
    }

    @Override
    public AssetData performConversion(StageElement input, ConfigArrayNode arguments) throws AssetPipelineException {
        return input.getAssetData();
    }
}
