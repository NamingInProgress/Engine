package com.vke.core.parsing.config.schema.elements;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.Configs;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.parsing.config.schema.VkeSchemaLib;
import com.vke.core.parsing.config.schema.elements.types.SchemaType;
import com.vke.utils.Identifier;
import com.vke.utils.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaHeader {
    private final HashMap<String, SchemaType> typedefs;

    public SchemaHeader(ConfigNode node) throws ConfigParser.ConfigParseException {
        typedefs = new HashMap<>();
        ConfigArrayNode links = Configs.getArray(node, "link");

        if (links != null) {
            List<String> linkPaths = Configs.getStringList(links);
            for (String path : linkPaths) {
                Identifier ident = Identifier.of(path);
                String filename = ident.getPath();
                ConfigParser parser = ConfigParser.forFileType(filename);
                if (parser == null) continue;
                try {
                    char[] source = Utils.readCharsFromInputStream(ident.asInputStream());
                    parser.setSource(source);
                    ConfigDocument libDoc = parser.parse();
                    VkeSchemaLib lib = new VkeSchemaLib(libDoc);
                    List<SchemaTypedef> types = lib.getTypedefs();
                    for (SchemaTypedef type : types) {
                        this.typedefs.put(type.getName(), type.getDefinition());
                    }
                } catch (IOException e) {
                    throw new ConfigParser.ConfigParseException(e);
                }
            }
        }

        ConfigArrayNode arr = Configs.getArray(node, "typedefs");
        if (arr != null) {
            for (ConfigNode tdNode : arr.values()) {
                SchemaTypedef typedef = new SchemaTypedef(tdNode, this);
                typedefs.put(typedef.getName(), typedef.getDefinition());
            }
        }
    }

    public SchemaType getTypeDef(String name) {
        return typedefs.get(name);
    }

    public Map<String, ? extends SchemaType> getTypedefs() {
        return typedefs;
    }
}
