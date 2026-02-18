package com.vke.core.parsing.config.json.nodes;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.ConfigObjectNode;

import java.util.HashMap;
import java.util.Map;

public class JsonObjectNode implements ConfigObjectNode {
    private final HashMap<String, ConfigNode> nodes;

    public JsonObjectNode() {
        nodes = new HashMap<>();
    }

    public void addNode(String key, ConfigNode node) {
        nodes.put(key, node);
    }

    @Override
    public ConfigNode getNode(String key) {
        return nodes.get(key);
    }

    @Override
    public Map<String, ? extends ConfigNode> getDescendants() {
        return nodes;
    }

    @Override
    public String toString() {
        return nodes.toString();
    }
}
