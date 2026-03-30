package com.vke.api.parsing.config.node;

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
