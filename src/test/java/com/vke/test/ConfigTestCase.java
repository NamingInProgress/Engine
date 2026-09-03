package com.vke.test;

import com.vke.api.app.Namespace;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.schema.ConfigSchema;
import com.vke.api.parsing.config.schema.SchemaMismatchException;
import com.vke.core.ModuleContext;

import java.io.IOException;

public class ConfigTestCase {
    public static void main(String[] args) throws SchemaMismatchException, IOException, ConfigParser.ConfigParseException {
        ModuleContext ctx = new ModuleContext(Namespace.of("lol"), null);
        ConfigDocument schemaDoc = ConfigDocument.parseIdentifier(ctx.fid("person.vks"));
        ConfigSchema schema = ConfigSchema.readVke(schemaDoc, "person.vks"); //filename here for good errors

        ConfigDocument doc = ConfigDocument.parseIdentifier(ctx.fid("person.json"));
        doc.validate(schema, "person.json"); //again here for good errors
    }
}
