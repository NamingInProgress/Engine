package com.vke.core.parsing.config.utils;

import com.vke.api.parsing.config.node.ConfigValueNode;

public record StringConfigValue(String s) implements ConfigValueNode {
    @Override
    public String getValue() {
        return s;
    }
}
