package com.vke.core.parsing.config.json;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigNode;

public class JsonDocument implements ConfigDocument {
    private ConfigNode root;

    public JsonDocument(ConfigNode root) {
        this.root = root;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public ConfigNode getRoot() {
        return root;
    }
}
