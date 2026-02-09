package com.vke.api.parsing.config.node;

import com.vke.api.parsing.config.node.ConfigNode;

import java.util.Map;

public interface AttributedConfigNode extends ConfigNode {
    Map<String, String> attributes();

    default String getAttribute(String key) {
        return attributes().get(key);
    }

    default boolean hasAttribute(String key) {
        return attributes().containsKey(key);
    }
}
