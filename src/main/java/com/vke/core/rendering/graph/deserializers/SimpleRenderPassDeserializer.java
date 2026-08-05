package com.vke.core.rendering.graph.deserializers;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.rendering.graph.def.RenderPassDefinition;

public class SimpleRenderPassDeserializer extends RenderPassDeserializer {

    private final Class<?> clazz;

    public SimpleRenderPassDeserializer(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public RenderPassDefinition accept(ConfigNode node, String name) throws ClassNotFoundException {
        return new RenderPassDefinition(name, clazz,
                buildInputTextures(node.getArray("inputs")),
                buildOutputTextures(node.getArray("outputs")));
    }
}
