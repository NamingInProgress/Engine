package com.vke.core.parsing.config.xml.nodes;

import com.vke.api.parsing.config.node.*;

import java.util.*;

public class XmlTagNode implements NamedConfigObjectNode, ConfigArrayNode, WriteAttribNode {
    private final String name;
    private final HashMap<String, String> attributes;
    private final HashMap<String, XmlPseudoArrayNode> children;
    private ConfigNode[] allValues;
    private List<ConfigNode> tmp;

    public XmlTagNode(String name) {
        this.name = name;
        attributes = new HashMap<>();
        children = new HashMap<>();
        tmp = new ArrayList<>();
    }

    @Override
    public Type getType() {
        return Type.Object;
    }

    @Override
    public void addAttrib(String key, String value) {
        attributes.put(key, value);
    }

    public void addNode(String key, ConfigNode node) {
        children.computeIfAbsent(key, _ -> new XmlPseudoArrayNode()).addNode(node);
        tmp.add(node);
    }

    public void finish() {
        children.values().forEach(XmlPseudoArrayNode::finish);
        allValues = tmp.toArray(new ConfigNode[0]);
        tmp = null;
    }

    @Override
    public ConfigNode getNode(String key) {
        XmlPseudoArrayNode node = children.get(key);
        if (node != null) {
            ConfigNode[] vals = node.values();
            if (vals.length >= 1) {
                return vals[0];
            }
        }
        return null;
    }

    @Override
    public Map<String, ? extends ConfigNode> getDescendants() {
        return children;
    }

    @Override
    public Map<String, String> attributes() {
        return attributes;
    }

    @Override
    public ConfigNode[] values() {
        return allValues;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "{%s attr %s: %s}".formatted(name, attributes.toString(), Arrays.toString(values()));
    }
}
