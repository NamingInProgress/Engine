package com.vke.core.parsing.config.xml.nodes;

import com.vke.api.parsing.config.node.ConfigBooleanNode;

public class XmlBooleanNode implements ConfigBooleanNode {
    private final boolean value;

    public XmlBooleanNode(boolean value) {
        this.value = value;
    }

    @Override
    public boolean getBoolean() {
        return value;
    }

    @Override
    public Type getType() {
        return Type.Boolean;
    }
}
