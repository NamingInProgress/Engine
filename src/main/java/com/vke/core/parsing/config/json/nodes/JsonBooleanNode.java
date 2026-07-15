package com.vke.core.parsing.config.json.nodes;

import com.vke.api.parsing.config.node.ConfigBooleanNode;

public class JsonBooleanNode implements ConfigBooleanNode {
    private final boolean value;

    public JsonBooleanNode(boolean value) {
        this.value = value;
    }

    @Override
    public boolean getBoolean() {
        return value;
    }

    @Override
    public Type getType() {
        return Type.Boolean;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
