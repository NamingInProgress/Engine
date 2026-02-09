package com.vke.api.parsing.config;

import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.ConfigObjectNode;

import java.nio.file.Path;

public interface ConfigDocument {
    String getName();

    ConfigNode getRoot();

    default ConfigNode resolve(String... path) {
        ConfigNode current = getRoot();
        for (String seg : path) {
            current = ((ConfigObjectNode) current).getNode(seg);
        }
        return current;
    }

    default ConfigArrayNode getArray(String... path) {
        return (ConfigArrayNode) resolve(path);
    }
}
