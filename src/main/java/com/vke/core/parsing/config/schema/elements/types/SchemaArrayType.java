package com.vke.core.parsing.config.schema.elements.types;

import com.vke.api.parsing.config.Configs;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.schema.SchemaValidationResult;
import com.vke.core.parsing.config.schema.JsonMarker;
import com.vke.core.parsing.config.schema.elements.SchemaCtx;

import java.util.ArrayDeque;

public class SchemaArrayType extends SchemaType {
    @JsonMarker("items")
    private SchemaType itemsType;

    public SchemaArrayType(ConfigNode node, SchemaCtx ctx) {
        super(node, ctx);
        this.type = Type.Array;
        this.itemsType = SchemaType.getType(Configs.getObject(node, "items"), ctx);
    }

    @Override
    public void validate(ConfigNode node, SchemaValidationResult result, ArrayDeque<String> path) {
        if (node instanceof ConfigArrayNode arrayNode) {
            int i = 0;
            for (ConfigNode value : arrayNode.values()) {
                String idx = String.format("[%d]", i++);
                path.addLast(idx);
                itemsType.validate(value, result, path);
                path.removeLast();
            }
        } else {
            result.addError(SchemaValidationResult.ValidationError.illegalType(node, ConfigNode.Type.Array, path));
        }
    }

    public SchemaType getItemsType() {
        return itemsType;
    }
}
