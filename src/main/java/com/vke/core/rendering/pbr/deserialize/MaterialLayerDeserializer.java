package com.vke.core.rendering.pbr.deserialize;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.rendering.pbr.MaterialLayer;
import com.vke.core.Context;

public abstract class MaterialLayerDeserializer<T extends MaterialLayer> {

    public abstract T accept(Context context, ConfigNode node);

}
