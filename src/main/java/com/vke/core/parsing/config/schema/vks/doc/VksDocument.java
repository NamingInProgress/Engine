package com.vke.core.parsing.config.schema.vks.doc;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.parsing.config.schema.vks.parser.VksPullParser;
import com.vke.utils.io.Identifier;

import java.io.IOException;

public class VksDocument implements ConfigDocument {
    private final VksSchema schema;
    private final String name;

    public VksDocument(Identifier identifier) throws IOException {
        VksPullParser parser = new VksPullParser(identifier);
        this.schema = new VksSchema(parser);
        this.name = identifier.getPath();
    }

    public VksDocument(char[] source) throws IOException {
        VksPullParser parser = new VksPullParser(source);
        this.schema = new VksSchema(parser);
        this.name = "";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ConfigNode getRoot() {
        return schema;
    }
}
