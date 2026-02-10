package com.vke.core.parsing.config.schema.elements.types;

import com.vke.api.parsing.config.node.ConfigBooleanNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.schema.SchemaValidationResult;
import com.vke.core.parsing.config.schema.elements.SchemaHeader;

import java.util.ArrayDeque;

public class SchemaBoolType extends SchemaType {
    public SchemaBoolType(ConfigNode node, SchemaHeader ctx) {
        super(node, ctx);
        this.type = Type.Boolean;
    }

    @Override
    public void validate(ConfigNode node, SchemaValidationResult result, ArrayDeque<String> path) {
        if (!(node instanceof ConfigBooleanNode)) {
            result.addError(SchemaValidationResult.ValidationError.illegalType(node, ConfigNode.Type.Boolean, path));
        }
    }
}
