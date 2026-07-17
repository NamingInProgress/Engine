package com.vke.core.parsing.config.xml.nodes;

import com.vke.api.parsing.config.node.*;

import java.util.ArrayList;
import java.util.List;

public class XmlPseudoArrayNode implements ConfigArrayNode, ConfigValueNode, ConfigNumberNode, ConfigBooleanNode {
    private List<ConfigNode> nodes;
    private ConfigNode[] arr;

    public XmlPseudoArrayNode() {
        nodes = new ArrayList<>();
    }

    public void addNode(ConfigNode node) {
        nodes.add(node);
    }

    public void finish() {
        arr = nodes.toArray(new ConfigNode[0]);
        nodes = null;
    }

    @Override
    public ConfigNode[] values() {
        return arr;
    }

    @Override
    public boolean getBoolean() {
        return arr[0].asBoolean();
    }

    @Override
    public float getNumber() {
        return arr[0].asNumber();
    }

    @Override
    public String getValue() {
        return arr[0].asString();
    }

    @Override
    public Type getType() {
        return ConfigArrayNode.super.getType();
    }
}
