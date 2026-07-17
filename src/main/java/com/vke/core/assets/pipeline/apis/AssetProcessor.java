package com.vke.core.assets.pipeline.apis;

import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.core.Context;
import com.vke.core.assets.pipeline.StageElement;

public interface AssetProcessor {
    String getName();

    /// This method should always set the inputs asset data to resolvedData. It can also modify or read that data before applying it.
    void process(Context context, StageElement input, AssetData resolvedData, ConfigArrayNode arguments);
}
