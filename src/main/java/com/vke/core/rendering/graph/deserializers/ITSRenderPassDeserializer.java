package com.vke.core.rendering.graph.deserializers;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.rendering.graph.def.ITSRenderPassDefinition;
import com.vke.core.rendering.graph.def.RenderPassDefinition;

public class ITSRenderPassDeserializer extends RenderPassDeserializer {
    @Override
    public RenderPassDefinition accept(ConfigNode node, String name) throws ClassNotFoundException {
        return new ITSRenderPassDefinition(name, node.getString("source"));
    }
}
