package com.vke.core.rendering.graph.deserializers;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.rendering.abstraction.renderer.enums.texture.Format;
import com.vke.core.Identifier;
import com.vke.core.rendering.graph.def.RenderPassDefinition;
import com.vke.core.rendering.graph.def.PostRenderPassDefinition;

import java.util.ArrayList;
import java.util.List;

public class PostRenderPassDeserializer extends RenderPassDeserializer {

    @Override
    public RenderPassDefinition accept(ConfigNode node, String name) throws ClassNotFoundException {
        List<RenderPassDefinition.OutputTextureDefinition> outputs = buildOutputTextures(node.getArray("outputs"));

        outputs.add(new RenderPassDefinition.OutputTextureDefinition(
                "colorOutPing",
                null,
                RenderPassDefinition.TextureType.COLOR,
                Format.RGBA16F,
                0,
                0,
                1
        ));

        return new PostRenderPassDefinition(name,
                buildInputTextures(node.getArray("inputs")),
                outputs,
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
