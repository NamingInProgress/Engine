package com.vke.core.parsing.config.schema.elements;

import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.Configs;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.parsing.config.json.JsonParser;
import com.vke.core.parsing.config.schema.elements.types.SchemaType;
import com.vke.utils.Identifier;

import java.util.HashMap;
import java.util.List;

public class SchemaCtx {
    private final HashMap<String, SchemaType> typedefs;

    public SchemaCtx(ConfigNode node) {
        typedefs = new HashMap<>();
        ConfigArrayNode links = Configs.getArray(node, "link");

        if (links != null) {
            ConfigParser libParser = new JsonParser();

            List<String> linkPaths = Configs.getStringList(links);
            for (String path : linkPaths) {
                Identifier ident = Identifier.of(path);

            }
        }

        ConfigArrayNode arr = Configs.getArray(node, "typedefs");
        if (arr != null) {
            for (ConfigNode tdNode : arr.values()) {
                SchemaTypedef typedef = new SchemaTypedef(tdNode, this);
                typedefs.put(typedef.getName(), typedef.getDefinition());
            }
        }
    }

    public SchemaType getTypeDef(String name) {
        return typedefs.get(name);
    }
}
