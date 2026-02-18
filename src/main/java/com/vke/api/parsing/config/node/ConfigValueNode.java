package com.vke.api.parsing.config.node;

public interface ConfigValueNode extends ConfigNode {
    String getValue();

    @Override
    default Type getType() {
        return Type.Value;
    }
}
