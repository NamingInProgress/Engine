package com.vke.core.parsing.config.xml.nodes;

import com.vke.api.parsing.config.node.NamedConfigNode;

import java.util.HashMap;
import java.util.Map;

public class XmlMetaNode implements WriteAttribNode, NamedConfigNode {
    private final String name;
    private final HashMap<String, String> attributes;

    public XmlMetaNode(String name) {
        this.name = name;
        attributes = new HashMap<>();
    }

    @Override
    public void addAttrib(String key, String attrib) {
        attributes.put(key, attrib);
    }

    @Override
    public Map<String, String> attributes() {
        return attributes;
    }

    @Override
    public Type getType() {
        return Type.Meta;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "{? %s: %s}".formatted(name, attributes.toString());
    }
}
