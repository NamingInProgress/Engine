package com.vke.core.parsing.config.json.nodes;

import com.vke.api.parsing.config.node.ConfigValueNode;

public class JsonValueNode implements ConfigValueNode {
    private String value;

    public JsonValueNode(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return '"' + value + '"';
    }
}
