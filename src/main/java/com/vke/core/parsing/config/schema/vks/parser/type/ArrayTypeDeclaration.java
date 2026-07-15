package com.vke.core.parsing.config.schema.vks.parser.type;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.ConfigObjectNode;
import com.vke.core.parsing.config.utils.StringConfigValue;

import java.util.Map;

public class ArrayTypeDeclaration implements VksTypeDeclaration{
    private final VksTypeDeclaration inner;

    public ArrayTypeDeclaration(VksTypeDeclaration inner) {
        this.inner = inner;
    }

    @Override
    public Map<String, ? extends ConfigNode> buildDescendantsMap() {
        return Map.of("type", new StringConfigValue("array"), "items", new ItemObject());
    }

    private class ItemObject implements ConfigObjectNode {
        @Override
        public ConfigNode getNode(String key) {
            return getDescendants().get(key);
        }

        @Override
        public Map<String, ? extends ConfigNode> getDescendants() {
            return inner.buildDescendantsMap();
        }
    }
}
