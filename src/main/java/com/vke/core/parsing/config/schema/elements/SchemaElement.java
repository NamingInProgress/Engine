package com.vke.core.parsing.config.schema.elements;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.schema.SchemaValidationResult;

import java.util.ArrayDeque;

public abstract class SchemaElement {
    public SchemaElement(ConfigNode node, SchemaCtx ctx) {

    }

    public abstract void validate(ConfigNode node, SchemaValidationResult result, ArrayDeque<String> path);
}
