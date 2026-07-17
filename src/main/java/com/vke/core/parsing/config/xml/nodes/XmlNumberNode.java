package com.vke.core.parsing.config.xml.nodes;

import com.vke.api.parsing.config.node.ConfigNumberNode;

public class XmlNumberNode implements ConfigNumberNode {
    private final float value;

    public XmlNumberNode(float value) {
        this.value = value;
    }

    @Override
    public float getNumber() {
        return value;
    }
}
