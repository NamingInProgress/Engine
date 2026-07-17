package com.vke.core.assets.pipeline.converters.pipeline;

import com.vke.api.assets.Protocols;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.core.Context;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetConverter;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.protocols.pipeline.ComputePipelineProtocol;

public class ConfigComputePipelineConverter implements AssetConverter {
    @Override
    public String from() {
        return Protocols.CONFIG;
    }

    @Override
    public String to() {
        return Protocols.COMPUTE_PIPELINE;
    }

    @Override
    public AssetData performConversion(Context context, StageElement input, ConfigArrayNode arguments) throws AssetException {
        ConfigDocument document = input.getAssetData().getDataAs();
        return ComputePipelineProtocol.fromConfig(context, document);
    }
}
