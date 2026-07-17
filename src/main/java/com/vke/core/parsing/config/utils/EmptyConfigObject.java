package com.vke.core.parsing.config.utils;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.ConfigObjectNode;

import java.util.Map;

public class EmptyConfigObject implements ConfigObjectNode {
    @Override
    public ConfigNode getNode(String key) {
        return null;
    }

    @Override
    public Map<String, ? extends ConfigNode> getDescendants() {
        return Map.of();
    }
}
