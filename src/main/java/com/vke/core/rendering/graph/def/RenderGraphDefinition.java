package com.vke.core.rendering.graph.def;

import com.vke.api.assets.r.R;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.ConfigObjectNode;
import com.vke.api.parsing.config.schema.ConfigSchema;
import com.vke.api.parsing.config.schema.SchemaMismatchException;
import com.vke.core.rendering.graph.deserializers.GeneralRenderPassDeserializer;
import com.vke.core.rendering.graph.deserializers.PostRenderPassDeserializer;
import com.vke.core.rendering.graph.deserializers.RenderPassDeserializer;
import com.vke.core.Context;
import com.vke.core.assets.handles.LazyAssetHandle;
import com.vke.core.rendering.pbr.PbrRenderPassDeserializer;
import com.vke.utils.io.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RenderGraphDefinition {

    private static final LazyAssetHandle<ConfigSchema> SCHEMA = R.schemas.get("render-graph.schema.json");
    private static final HashMap<String, RenderPassDeserializer> DESERIALIZERS = new HashMap<>(); // TODO: Change to registry

    static {
        DESERIALIZERS.put("general", new GeneralRenderPassDeserializer());
        DESERIALIZERS.put("post", new PostRenderPassDeserializer());
        DESERIALIZERS.put("pbr", new PbrRenderPassDeserializer());
    }

    public final List<RenderPassDefinition> renderPasses = new ArrayList<>();

    public RenderGraphDefinition(Context context, Identifier id) throws IOException, SchemaMismatchException, ClassNotFoundException {
        ConfigDocument doc = ConfigDocument.parseIdentifier(id);
        //doc.validate(SCHEMA.assume(context), id.toString());

        ConfigObjectNode root = doc.getRoot().getObject("render-graph");
        for (ConfigNode node : root.asArray().values()) {
            if (!(node instanceof ConfigObjectNode)) continue;
            String name = node.getString("name");
            String parent = node.getStringOption("parent").unwrapOr("general");

            RenderPassDeserializer deserializer = DESERIALIZERS.get(parent);
            renderPasses.add(deserializer.accept(node, name));
        }
    }

}
