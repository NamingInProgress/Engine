package com.vke.core.parsing.config.xml.nodes;

import com.vke.api.parsing.config.node.ConfigValueNode;

public class XmlValueNode implements ConfigValueNode {
    private final String value;

    public XmlValueNode(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }
}
