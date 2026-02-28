package com.vke.api.parsing.config.node;

public class EmptyConfigArray implements ConfigArrayNode {
    @Override
    public ConfigNode[] values() {
        return new ConfigNode[0];
    }
}
