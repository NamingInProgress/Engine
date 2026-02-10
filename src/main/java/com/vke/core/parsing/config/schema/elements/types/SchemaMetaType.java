package com.vke.core.parsing.config.schema.elements.types;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.schema.SchemaValidationResult;
import com.vke.core.parsing.config.schema.elements.SchemaCtx;

import java.util.ArrayDeque;

public class SchemaMetaType extends SchemaType {
    public SchemaMetaType(ConfigNode node, SchemaCtx ctx) {
        super(node, ctx);
    }

    @Override
    public void validate(ConfigNode node, SchemaValidationResult result, ArrayDeque<String> path) {
        //this is always valid
    }
}
