package com.vke.core.parsing.config.schema.elements.types;

import com.vke.api.parsing.config.Configs;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.ConfigObjectNode;
import com.vke.api.parsing.config.schema.SchemaValidationResult;
import com.vke.core.parsing.config.schema.JsonMarker;
import com.vke.core.parsing.config.schema.elements.SchemaCtx;
import com.vke.core.parsing.config.schema.elements.SchemaField;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class SchemaObjectType extends SchemaType {
    @JsonMarker("fields")
    private final List<SchemaField> fields;

    public SchemaObjectType(ConfigNode node, SchemaCtx ctx) {
        super(node, ctx);
        this.type = Type.Object;
        this.fields = new ArrayList<>();
        ConfigArrayNode fieldsArray = Configs.getArray(node, "fields");
        for (ConfigNode fieldNode : fieldsArray.values()) {
            SchemaField field = new SchemaField(fieldNode, ctx);
            this.fields.add(field);
        }
    }

    @Override
    public void validate(ConfigNode node, SchemaValidationResult result, ArrayDeque<String> path) {
        if (node instanceof ConfigObjectNode objectNode) {
            for (String fieldName : objectNode.getDescendants().keySet()) {
                if (fields.stream().noneMatch(f -> f.getName().equals(fieldName))) {
                    result.addError(SchemaValidationResult.ValidationError.illegalField(fieldName, path));
                }
            }
            for (SchemaField fieldDef : fields) {
                String name = fieldDef.getName();
                boolean exists = Configs.hasField(node, name);
                if (!exists) {
                    if (fieldDef.isRequired()) {
                        result.addError(SchemaValidationResult.ValidationError.missingField(path, name));
                    }
                } else {
                    fieldDef.validate(objectNode.getNode(name), result, path);
                }
            }
        } else {
            result.addError(SchemaValidationResult.ValidationError.illegalType(node, ConfigNode.Type.Object, path));
        }
    }

    public SchemaObjectType(List<SchemaField> fields) {
        super(null, null);
        this.type = Type.Object;
        this.fields = fields;
    }

    public List<SchemaField> getFields() {
        return fields;
    }
}
