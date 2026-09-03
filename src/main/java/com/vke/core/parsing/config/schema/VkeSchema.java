package com.vke.core.parsing.config.schema;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.Configs;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.schema.ConfigSchema;
import com.vke.api.parsing.config.schema.SchemaElementLocation;
import com.vke.api.parsing.config.schema.SchemaMismatchException;
import com.vke.api.parsing.config.schema.SchemaValidationResult;
import com.vke.core.FileIdentifier;
import com.vke.core.parsing.config.json.JsonParser;
import com.vke.core.parsing.config.schema.elements.SchemaHeader;
import com.vke.core.parsing.config.schema.elements.SchemaField;
import com.vke.core.parsing.config.schema.elements.types.SchemaObjectType;
import com.vke.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class VkeSchema implements ConfigSchema {
    private static final ConfigSchema masterSchema;
    static {
        try {
            FileIdentifier ident = FileIdentifier.of("schema/master/master.schema.json");
            char[] source = Utils.readCharsFromInputStream(ident.openInputStream());
            ConfigParser parser = new JsonParser();
            parser.setSource(source);
            ConfigDocument d = parser.parse();
            masterSchema = new VkeSchema(d, "master", false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final SchemaObjectType root;

    public VkeSchema(ConfigDocument schemaDoc, String filename) throws ConfigParser.ConfigParseException {
        this(schemaDoc, filename, true);
    }

    private VkeSchema(ConfigDocument schemaDoc, String filename, boolean validate) throws ConfigParser.ConfigParseException {
        if (validate) {
            try {
                schemaDoc.validate(masterSchema, filename);
            } catch (SchemaMismatchException e) {
                throw new ConfigParser.ConfigParseException(e);
            }
        }

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
    public SchemaValidationResult validate(ConfigNode root, String filename) {
        SchemaValidationResult result = new SchemaValidationResult();
        SchemaElementLocation location = new SchemaElementLocation(filename);
        this.root.validate(root, result, location);
        return result;
    }
}
