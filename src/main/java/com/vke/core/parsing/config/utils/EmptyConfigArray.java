package com.vke.core.parsing.config.utils;

import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;

public class EmptyConfigArray implements ConfigArrayNode {
    @Override
    public ConfigNode[] values() {
        return new ConfigNode[0];
    }
}
