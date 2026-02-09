package com.vke.api.parsing.config.node;

public interface ConfigNumberNode extends ConfigNode {
    float getValue();

    @Override
    default Type getType() {
        return Type.Number;
    }
}
