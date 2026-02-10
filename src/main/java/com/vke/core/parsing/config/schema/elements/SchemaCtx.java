package com.vke.core.parsing.config.schema.elements;

import com.vke.api.parsing.config.Configs;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.parsing.config.schema.elements.types.SchemaType;

import java.util.HashMap;

public class SchemaCtx {
    private HashMap<String, SchemaType> typedefs;

    public SchemaCtx(ConfigNode node) {
        typedefs = new HashMap<>();
        ConfigArrayNode arr = Configs.getArray(node, "typedefs");
        for (ConfigNode tdNode : arr.values()) {
            SchemaTypedef typedef = new SchemaTypedef(tdNode, this);
            typedefs.put(typedef.getName(), typedef.getDefinition());
        }
    }

    public SchemaType getTypeDef(String name) {
        return typedefs.get(name);
    }
}
