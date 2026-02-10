package com.vke.core.parsing.config.schema;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.Configs;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.schema.ConfigSchema;
import com.vke.api.parsing.config.schema.SchemaValidationResult;
import com.vke.core.parsing.config.schema.elements.SchemaHeader;
import com.vke.core.parsing.config.schema.elements.SchemaField;
import com.vke.core.parsing.config.schema.elements.types.SchemaObjectType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class VkeSchema implements ConfigSchema {
    private final SchemaObjectType root;

    public VkeSchema(ConfigDocument schemaDoc) throws ConfigParser.ConfigParseException {
        ConfigNode root = schemaDoc.getRoot();
        SchemaHeader schemaHeader = new SchemaHeader(root);
        ConfigArrayNode fieldsArray = Configs.getArray(root, "fields");

        List<SchemaField> fields = new ArrayList<>(fieldsArray.values().length);
        for (ConfigNode fieldNode : fieldsArray.values()) {
            SchemaField field = new SchemaField(fieldNode, schemaHeader);
            fields.add(field);
        }

        this.root = new SchemaObjectType(fields);
    }

    @Override
    public SchemaValidationResult validate(ConfigNode root) {
        SchemaValidationResult result = new SchemaValidationResult();
        this.root.validate(root, result, new ArrayDeque<>());
        return result;
    }
}
