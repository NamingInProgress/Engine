package com.vke.core.parsing.config.schema;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.core.parsing.config.schema.elements.SchemaHeader;
import com.vke.core.parsing.config.schema.elements.SchemaTypedef;

import java.util.ArrayList;
import java.util.List;

public class VkeSchemaLib {
    //private final List<SchemaTypedef> typedefs;

    public VkeSchemaLib(ConfigDocument schemaDoc) throws ConfigParser.ConfigParseException {
        this.typedefs = new ArrayList<>();

        SchemaHeader header = new SchemaHeader(schemaDoc.getRoot());
        var allDefs = header.getTypedefs();
        allDefs.forEach((name, def) -> {
            typedefs.add(new SchemaTypedef(name, def));
        });
    }

    public List<SchemaTypedef> getTypedefs() {
        return typedefs;
    }
}
