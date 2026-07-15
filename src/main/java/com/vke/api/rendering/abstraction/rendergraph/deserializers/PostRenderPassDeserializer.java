package com.vke.api.rendering.abstraction.rendergraph.deserializers;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.rendering.abstraction.rendergraph.RenderPassDefinition;
import com.vke.api.rendering.abstraction.rendergraph.def.PostRenderPassDefinition;

import java.util.ArrayList;

public class PostRenderPassDeserializer extends RenderPassDeserializer {

    @Override
    public RenderPassDefinition accept(ConfigNode node, String name) throws ClassNotFoundException {
        return new PostRenderPassDefinition(name, null,
                buildInputTextures(node.getArray("inputs")),
                buildOutputTextures(node.getArray("outputs")),
                buildUniforms(node.getArray("uniforms")),
                buildPostStages(node.getArray("stages")));
    }

    public ArrayList<PostRenderPassDefinition.PostStage> buildPostStages(ConfigNode node) {
        ArrayList<PostRenderPassDefinition.PostStage> stages = new ArrayList<>();
        if (node == null) return stages;

        for (ConfigNode stage : node.asArray().values()) {
            stages.add(PostRenderPassDefinition.PostStage.fromString(stage.getNodeName()));
        }

        return stages;
    }

}
