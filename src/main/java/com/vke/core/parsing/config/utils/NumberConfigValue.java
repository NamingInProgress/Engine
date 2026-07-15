package com.vke.core.parsing.config.utils;

import com.vke.api.parsing.config.node.ConfigNumberNode;

public record NumberConfigValue(float value) implements ConfigNumberNode {
    @Override
    public float getValue() {
        return value;
    }
}
