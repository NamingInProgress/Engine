package com.vke.core.parsing.config.schema.elements.types;

import com.vke.api.parsing.config.Configs;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.ConfigNumberNode;
import com.vke.api.parsing.config.node.ConfigValueNode;
import com.vke.api.parsing.config.schema.SchemaValidationResult;
import com.vke.core.parsing.config.schema.JsonMarker;
import com.vke.core.parsing.config.schema.elements.SchemaCtx;

import java.util.ArrayDeque;
import java.util.List;

public class SchemaStringType extends SchemaType {
    @JsonMarker("enum")
    private final List<String> allowed;

    public SchemaStringType(ConfigNode node, SchemaCtx ctx) {
        super(node, ctx);
        this.type = Type.String;
        ConfigArrayNode arr = Configs.getArray(node, "enum");
        if (arr != null) {
            allowed = Configs.getStringList(arr);
        } else{
            allowed = null;
        }
    }

    @Override
    public void validate(ConfigNode node, SchemaValidationResult result, ArrayDeque<String> path) {
        if (node instanceof ConfigValueNode valueNode) {
            if (allowed != null) {
                String value = valueNode.getValue();
                if (!allowed.contains(value)) {
                    String message = String.format("\"%s\" is not an allowed enum value! Allowed values are: %s", value, allowed);
                    result.addError(new SchemaValidationResult.ValidationError(message, path));
                }
            }
        } else {
            result.addError(SchemaValidationResult.ValidationError.illegalType(node, ConfigNode.Type.Value, path));
        }
    }

    public List<String> getAllowed() {
        return allowed;
    }
}
