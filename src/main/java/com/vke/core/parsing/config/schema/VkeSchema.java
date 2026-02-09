package com.vke.core.parsing.config.schema;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.Configs;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.schema.ConfigSchema;
import com.vke.api.parsing.config.schema.SchemaMismatchException;
import com.vke.api.parsing.config.schema.SchemaValidationResult;
import com.vke.core.parsing.config.json.JsonParser;
import com.vke.core.parsing.config.schema.elements.SchemaCtx;
import com.vke.core.parsing.config.schema.elements.SchemaField;
import com.vke.core.parsing.config.schema.elements.types.SchemaObjectType;
import com.vke.utils.Identifier;
import com.vke.utils.Utils;

import java.io.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class VkeSchema implements ConfigSchema {
    private final SchemaObjectType root;

    public VkeSchema(ConfigDocument schemaDoc) {
        ConfigNode root = schemaDoc.getRoot();
        SchemaCtx schemaCtx = new SchemaCtx(root);
        ConfigArrayNode fieldsArray = Configs.getArray(root, "fields");

        List<SchemaField> fields = new ArrayList<>(fieldsArray.values().length);
        for (ConfigNode fieldNode : fieldsArray.values()) {
            SchemaField field = new SchemaField(fieldNode, schemaCtx);
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
