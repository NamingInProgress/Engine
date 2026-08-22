package com.vke.core.rendering.pbr.deserialize;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.ConfigObjectNode;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.rendering.pbr.Material;
import com.vke.api.rendering.pbr.MaterialLayer;
import com.vke.core.Context;
import com.vke.core.Identifier;
import com.vke.core.services2.Services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MaterialDeserializers {

    private static final HashMap<String, MaterialLayerDeserializer<?>> DESERIALIZERS = new HashMap<>();

    static {
        DESERIALIZERS.put("base-material", new BaseMaterialLayerDeserializer());
    }

    public static Material parse(Context ctx, Identifier id, ConfigDocument doc) {
        //doc.validate(SCHEMA.assume(ctx, id.toString()));

        ConfigObjectNode materialNode = doc.getRoot().getObject("material");
        List<MaterialLayer> layers = new ArrayList<>();
        for (ConfigNode value : materialNode.asArray().values()) {
            if (!(value instanceof ConfigObjectNode)) continue;

            String parent = value.getStringOption("parent")
                    .unwrapOrPanic(new IllegalArgumentException("Material layer must have a parent!"));

            MaterialLayerDeserializer<?> deserializer = DESERIALIZERS.get(parent);
            layers.add(deserializer.accept(value));
        }

        return new Material(ctx.<Renderer>service(Services.RENDERER).renderSystem(), layers);
    }

}
