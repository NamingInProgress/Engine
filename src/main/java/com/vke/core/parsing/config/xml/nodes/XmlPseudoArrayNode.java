package com.vke.core.parsing.config.xml.nodes;

import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;

import java.util.ArrayList;
import java.util.List;

public class XmlPseudoArrayNode implements ConfigArrayNode {
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
}
