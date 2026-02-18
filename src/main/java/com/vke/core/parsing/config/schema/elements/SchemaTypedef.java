package com.vke.core.parsing.config.schema.elements;

import com.vke.api.parsing.config.Configs;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.parsing.config.schema.elements.types.SchemaType;

public class SchemaTypedef {
    private String name;
    private SchemaType definition;

    public SchemaTypedef(ConfigNode node, SchemaHeader ctx) {
        this.name = Configs.getString(node, "name");
        this.definition = SchemaType.getType(node, ctx);
    }

    public SchemaTypedef(String name, SchemaType definition) {
        this.name = name;
        this.definition = definition;
    }

    public String getName() {
        return name;
    }

    public SchemaType getDefinition() {
        return definition;
    }
}
