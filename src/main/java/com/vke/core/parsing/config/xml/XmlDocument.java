package com.vke.core.parsing.config.xml;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigNode;

public class XmlDocument implements ConfigDocument {
    private ConfigNode root;

    public XmlDocument(ConfigNode root) {
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
