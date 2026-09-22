package com.vke.core.parsing.config.schema.vks.doc;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.FileIdentifier;
import com.vke.core.parsing.config.schema.vks.parser.VksPullParser;

import java.io.IOException;

public class VksDocument implements ConfigDocument {
    private final VksSchema schema;
    private final FileIdentifier identifier;

    public VksDocument(FileIdentifier identifier) throws IOException {
        VksPullParser parser = new VksPullParser(identifier);
        this.schema = new VksSchema(parser);
        this.identifier = identifier;
    }

    public VksDocument(char[] source) throws IOException {
        VksPullParser parser = new VksPullParser(source);
        this.schema = new VksSchema(parser);
        this.identifier = FileIdentifier.of("<unknown>");
    }

    public VksDocument(char[] source, FileIdentifier identifier) throws IOException {
        VksPullParser parser = new VksPullParser(source);
        this.schema = new VksSchema(parser);
        this.identifier = identifier;
    }

    @Override
    public FileIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public ConfigNode getRoot() {
        return schema;
    }
}
