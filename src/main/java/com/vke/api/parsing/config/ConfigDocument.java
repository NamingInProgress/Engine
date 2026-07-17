package com.vke.api.parsing.config;

import com.vke.api.assets.AssetHandle;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.ConfigObjectNode;
import com.vke.api.parsing.config.schema.ConfigSchema;
import com.vke.api.parsing.config.schema.SchemaMismatchException;
import com.vke.api.parsing.config.schema.SchemaValidationResult;
import com.vke.utils.io.Identifier;
import com.vke.utils.Utils;

import java.io.IOException;

public interface ConfigDocument {
    String getName();

    ConfigNode getRoot();

    default void validate(ConfigSchema schema, String filename) throws SchemaMismatchException {
        SchemaValidationResult result = schema.validate(getRoot(), filename);
        if (!result.isValid()) {
            StringBuilder error = new StringBuilder();
            error.append("There were validation errors when validating input with schema:");
            error.append(System.lineSeparator());
            for (SchemaValidationResult.ValidationError e : result.getErrors()) {
                error.append(e.getMessage());
                error.append(System.lineSeparator());
            }
            throw new SchemaMismatchException(error.toString());
        }
    }

    default ConfigNode resolve(String... path) {
        ConfigNode current = getRoot();
        for (String seg : path) {
            current = ((ConfigObjectNode) current).getNode(seg);
        }
        return current;
    }

    default ConfigArrayNode getArray(String... path) {
        return (ConfigArrayNode) resolve(path);
    }

    static ConfigDocument parseIdentifier(Identifier identifier) throws IOException {
        String filename = identifier.strip().getPath();
        ConfigParser parser = ConfigParser.forFileType(filename);
        if (parser == null) {
            throw new IOException("No suitable parser found for " + filename);
        }
        char[] source = Utils.readCharsFromInputStream(identifier.asInputStream());
        parser.setSource(source);
        try {
            return parser.parse(ConfigParser.PARSE_LITERALS | ConfigParser.ATTRIBS_TO_FIELDS);
        } catch (ConfigParser.ConfigParseException e) {
            throw new IOException(e);
        }
    }
}
