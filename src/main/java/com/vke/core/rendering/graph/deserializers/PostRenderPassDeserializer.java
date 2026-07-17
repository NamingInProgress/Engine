package com.vke.core.rendering.graph.deserializers;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.rendering.graph.def.RenderPassDefinition;
import com.vke.core.rendering.graph.def.PostRenderPassDefinition;
import com.vke.utils.io.Identifier;

import java.util.ArrayList;

public class PostRenderPassDeserializer extends RenderPassDeserializer {

    @Override
    public RenderPassDefinition accept(ConfigNode node, String name) throws ClassNotFoundException {
        return new PostRenderPassDefinition(name,
                buildInputTextures(node.getArray("inputs")),
                buildOutputTextures(node.getArray("outputs")),
                buildPostStages(node.getArray("stages")));
    }

    public ArrayList<Identifier> buildPostStages(ConfigNode node) {
        ArrayList<Identifier> stages = new ArrayList<>();
        if (node == null) return stages;

        for (ConfigNode stage : node.asArray().values()) {
            stages.add(Identifier.of(stage.getStringOption("name").unwrapOrPanic(new IllegalStateException("Cannot have stage without name tag!"))));
        }

        return stages;
    }

}
