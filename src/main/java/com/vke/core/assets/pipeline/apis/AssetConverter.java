package com.vke.core.assets.pipeline.apis;

import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.core.Context;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.StageElement;

public interface AssetConverter {
    String from();
    String to();
    AssetData performConversion(Context context, StageElement input, ConfigArrayNode arguments) throws AssetException;
}
