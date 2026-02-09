package com.vke.core.parsing.config.json.nodes;

import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;

import java.util.Arrays;

public class JsonArrayNode implements ConfigArrayNode {
    private final ConfigNode[] array;

    public JsonArrayNode(ConfigNode[] array) {
        this.array = array;
    }

    @Override
    public ConfigNode[] values() {
        return array;
    }

    @Override
    public String toString() {
        return Arrays.toString(array);
    }
}
