package com.vke.api.rendering.abstraction.rendergraph.deserializers;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.rendering.abstraction.rendergraph.RenderPassDefinition;

import java.util.HashMap;

public abstract class RenderPassDeserializer {
    public abstract RenderPassDefinition accept(ConfigNode node, String name) throws ClassNotFoundException;

    public HashMap<String, RenderPassDefinition.InputTextureDefinition> buildInputTextures(ConfigNode node) {
        HashMap<String, RenderPassDefinition.InputTextureDefinition> inputs = new HashMap<>();
        if (node == null) return inputs;

        for (ConfigNode input : node.asArray().values()) {
            String name = input.getString("name");
            String source = input.getString("source");
            String uniform = input.getStringOption("uniform-field-name").unwrapOrNull();
            int width = input.getIntOption("width").unwrapOrDefault();
            int height = input.getIntOption("height").unwrapOrDefault();
            float scale = input.getNumberOption("scale").unwrapOr(1f);

            inputs.put(name, new RenderPassDefinition.InputTextureDefinition(source, uniform, width, height, scale));
        }

        return inputs;
    }

    public HashMap<String, RenderPassDefinition.TextureType> buildOutputTextures(ConfigNode node) {
        HashMap<String, RenderPassDefinition.TextureType> outputs = new HashMap<>();
        if (node == null) return outputs;

        for (ConfigNode output : node.asArray().values()) {
            if (output.getNodeName().equalsIgnoreCase("render-target")) {
                outputs.put("render-target", RenderPassDefinition.TextureType.RENDER_TARGET);
                continue;
            }
            String name = output.getString("name");
            RenderPassDefinition.TextureType type = RenderPassDefinition.TextureType.fromString(output.getString("type"));

            outputs.put(name, type);
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
