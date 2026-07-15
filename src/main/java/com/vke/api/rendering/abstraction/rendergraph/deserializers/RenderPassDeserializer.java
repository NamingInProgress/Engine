package com.vke.api.rendering.abstraction.rendergraph.deserializers;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.rendering.abstraction.renderer.enums.texture.Format;
import com.vke.api.rendering.abstraction.rendergraph.def.RenderPassDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class RenderPassDeserializer {
    public abstract RenderPassDefinition accept(ConfigNode node, String name) throws ClassNotFoundException;

    public List<RenderPassDefinition.InputTextureDefinition> buildInputTextures(ConfigNode node) {
        List<RenderPassDefinition.InputTextureDefinition> inputs = new ArrayList<>();
        if (node == null) return inputs;

        for (ConfigNode input : node.asArray().values()) {
            String name = input.getString("name");
            String source = input.getStringOption("source").unwrapOrNull();
            String uniform = input.getStringOption("uniform-field-name").unwrapOrNull();

            inputs.add(new RenderPassDefinition.InputTextureDefinition(name, source, uniform));
        }

        return inputs;
    }

    public List<RenderPassDefinition.OutputTextureDefinition> buildOutputTextures(ConfigNode node) {
        List<RenderPassDefinition.OutputTextureDefinition> outputs = new ArrayList<>();
        if (node == null) return outputs;

        for (ConfigNode output : node.asArray().values()) {
            if (output.getNodeName().equalsIgnoreCase("render-target")) {
                outputs.add(new RenderPassDefinition.OutputTextureDefinition("render-target", null,
                        RenderPassDefinition.TextureType.RENDER_TARGET, Format.RGBA8_SRGB, 0, 0, 1));
                continue;
            }
            String name = output.getString("name");
            String source = output.getStringOption("source").unwrapOrNull();
            int width = output.getIntOption("width").unwrapOrDefault();
            int height = output.getIntOption("height").unwrapOrDefault();
            float scale = output.getNumberOption("scale").unwrapOr(1f);
            RenderPassDefinition.TextureType type = RenderPassDefinition.TextureType.fromString(output.getString("type"));
            Format format = Format.valueOfOption(output.getStringOption("format").unwrapOrNull()).unwrapOr(Format.RGBA8);

            outputs.add(new RenderPassDefinition.OutputTextureDefinition(name, source, type, format, width, height, scale));
        }

        return outputs;
    }

    public HashMap<String, String> buildUniforms(ConfigNode node) {
        HashMap<String, String> uniforms = new HashMap<>();
        if (node == null) return uniforms;

        for (ConfigNode uniform : node.asArray().values()) {
            uniforms.put(uniform.getString("field"), uniform.getString("path"));
        }

        return uniforms;
    }
}
