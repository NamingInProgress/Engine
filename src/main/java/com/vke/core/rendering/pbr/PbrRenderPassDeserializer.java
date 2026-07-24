package com.vke.core.rendering.pbr;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.rendering.graph.def.RenderPassDefinition;
import com.vke.core.rendering.graph.deserializers.RenderPassDeserializer;

public class PbrRenderPassDeserializer extends RenderPassDeserializer {
    @Override
    public RenderPassDefinition accept(ConfigNode node, String name) throws ClassNotFoundException {
        return null;
    }
}
