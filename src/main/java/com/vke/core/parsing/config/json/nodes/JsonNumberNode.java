package com.vke.core.parsing.config.json.nodes;

import com.vke.api.parsing.config.node.ConfigNumberNode;

public class JsonNumberNode implements ConfigNumberNode {
    private float value;

    public JsonNumberNode(float value) {
        this.value = value;
    }

    @Override
    public float getNumber() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
