package com.vke.core.parsing.config.schema.elements.types;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.schema.SchemaElementLocation;
import com.vke.api.parsing.config.schema.SchemaValidationResult;
import com.vke.core.parsing.config.schema.elements.SchemaHeader;

import java.util.ArrayDeque;

public class SchemaTypeReference extends SchemaType {
    private SchemaType resolved;

    public SchemaTypeReference(ConfigNode node, SchemaHeader header) {
        super(node, header);
        this.type = Type.Meta;
        this.resolved = null;
    }

    public void resolve(SchemaType resolved) {
        this.resolved = resolved;
    }

    @Override
    public void validate(ConfigNode node, SchemaValidationResult result, SchemaElementLocation path) {
        if (resolved == null) {
            result.addError(new SchemaValidationResult.ValidationError("Type has not been resolved yet (critical parsing bug)", path));
            return;
        }
        resolved.validate(node, result, path);
    }
}
