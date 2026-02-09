package com.vke.api.parsing.config.node;

public interface ConfigArrayNode extends ConfigNode {
    ConfigNode[] values();

    @Override
    default Type getType() {
        return Type.Array;
    }
}
