package com.vke.core.rendering.passes;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.rendering.abstraction.renderer.enums.texture.Format;
import com.vke.core.rendering.graph.def.RenderPassDefinition;
import com.vke.core.rendering.graph.deserializers.RenderPassDeserializer;

import java.util.List;

public class DeferredRenderPassDeserializer extends RenderPassDeserializer {
    @Override
    public RenderPassDefinition accept(ConfigNode node, String name) throws ClassNotFoundException {
        return new RenderPassDefinition(name, DeferredRenderPass.class,
                buildInputTextures(node.getArray("inputs")),
                buildGBufferAndOutput(node.getArray("outputs")));
    }

    public List<RenderPassDefinition.OutputTextureDefinition> buildGBufferAndOutput(ConfigNode node) {
        var list = buildOutputTextures(node);

        list.add(new RenderPassDefinition.OutputTextureDefinition(
                "gbuf_normal",
                null,
                RenderPassDefinition.TextureType.COLOR,
                Format.RGBA16F,
                0,
                0,
                1
        ));

        list.add(new RenderPassDefinition.OutputTextureDefinition(
                "gbuf_albedo_spec",
                null,
                RenderPassDefinition.TextureType.COLOR,
                Format.RGBA8_SRGB,
                0,
                0,
                1
        ));

        return list;
    }

}
