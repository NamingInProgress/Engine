package com.vke.core.rendering.graph.deserializers;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.rendering.graph.def.RenderPassDefinition;

public class GeneralRenderPassDeserializer extends RenderPassDeserializer {
    @Override
    public RenderPassDefinition accept(ConfigNode node, String name) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(node.getObject("class").getString("name"));

        return new RenderPassDefinition(name, clazz,
                buildInputTextures(node.getArray("inputs")),
                buildOutputTextures(node.getArray("outputs")));
    }
}
