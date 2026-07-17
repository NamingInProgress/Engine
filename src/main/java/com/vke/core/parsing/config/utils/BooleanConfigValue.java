package com.vke.core.parsing.config.utils;

import com.vke.api.parsing.config.node.ConfigBooleanNode;

public record BooleanConfigValue(boolean value) implements ConfigBooleanNode {
    @Override
    public boolean getBoolean() {
        return value;
    }

    @Override
    public Type getType() {
        return Type.Boolean;
    }
}
