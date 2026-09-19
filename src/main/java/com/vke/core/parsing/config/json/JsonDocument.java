package com.vke.core.parsing.config.json;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.FileIdentifier;

public class JsonDocument implements ConfigDocument {
    private final ConfigNode root;
    private final FileIdentifier identifier;

    public JsonDocument(ConfigNode root, FileIdentifier identifier) {
        this.root = root;
        this.identifier = identifier;
    }

    @Override
    public FileIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public ConfigNode getRoot() {
        return root;
    }
}
