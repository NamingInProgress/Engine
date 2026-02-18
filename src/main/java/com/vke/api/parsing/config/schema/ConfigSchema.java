package com.vke.api.parsing.config.schema;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.parsing.config.schema.VkeSchema;

public interface ConfigSchema {
    SchemaValidationResult validate(ConfigNode root, String filename);

    static ConfigSchema readVke(ConfigDocument schemaDocument, String filename) throws ConfigParser.ConfigParseException {
        return new VkeSchema(schemaDocument, filename);
    }
}
