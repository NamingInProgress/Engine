package com.vke.core.parsing.config.schema.elements;

import com.vke.api.parsing.config.Configs;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.schema.SchemaValidationResult;
import com.vke.core.parsing.config.schema.JsonMarker;
import com.vke.core.parsing.config.schema.elements.types.SchemaType;

import java.util.ArrayDeque;

public class SchemaField extends SchemaElement {
    @JsonMarker("name")
    private String name;
    @JsonMarker("type")
    private SchemaType type;
    @JsonMarker("required")
    private boolean required;

    public SchemaField(ConfigNode node, SchemaHeader ctx) {
        super(node, ctx);
        this.name = Configs.getString(node, "name");
        this.type = SchemaType.getType(node, ctx);
        this.required = Configs.getBoolean(node, "required");
    }

    @Override
    public void validate(ConfigNode node, SchemaValidationResult result, ArrayDeque<String> path) {
        path.addLast(name);
        type.validate(node, result, path);
        path.removeLast();
    }

    public String getName() {
        return name;
    }

    public SchemaType getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }
}
