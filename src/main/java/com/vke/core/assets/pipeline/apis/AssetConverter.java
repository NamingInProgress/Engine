package com.vke.core.assets.pipeline.apis;

import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.core.assets.pipeline.AssetPipelineException;
import com.vke.core.assets.pipeline.StageElement;

public interface AssetConverter {
    String from();
    String to();
    AssetData performConversion(StageElement input, ConfigArrayNode arguments) throws AssetPipelineException;
}
