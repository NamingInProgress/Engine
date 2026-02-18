package com.vke.api.parsing.config.node;

import java.util.Map;

public interface ConfigObjectNode extends ConfigNode {
    ConfigNode getNode(String key);
    Map<String, ? extends ConfigNode> getDescendants();

    @Override
    default Type getType() {
        return Type.Object;
    }
}
