package com.vke.core.parsing.config.utils;

import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;

public class GeneralConfigArrayNode implements ConfigArrayNode {
    private final ConfigNode[] arr;

    public GeneralConfigArrayNode(ConfigNode[] arr) {
        this.arr = arr;
    }

    @Override
    public ConfigNode[] values() {
        return arr;
    }
}
