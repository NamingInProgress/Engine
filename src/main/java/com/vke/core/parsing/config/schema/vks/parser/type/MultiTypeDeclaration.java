package com.vke.core.parsing.config.schema.vks.parser.type;

import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.ConfigObjectNode;
import com.vke.core.parsing.config.utils.StringConfigValue;

import java.util.List;
import java.util.Map;

public class MultiTypeDeclaration implements VksTypeDeclaration {
    private final List<VksTypeDeclaration> allowed;

    public MultiTypeDeclaration(List<VksTypeDeclaration> allowed) {
        this.allowed = allowed;
    }

    @Override
    public Map<String, ? extends ConfigNode> buildDescendantsMap() {
        return Map.of("type", new StringConfigValue("multi"), "allowed", new AllowedArray());
    }

    private class AllowedArray implements ConfigArrayNode {
        private final TypeObject[] arr;

        private AllowedArray() {
            arr = new TypeObject[allowed.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = new TypeObject(allowed.get(i));
            }
        }

        @Override
        public ConfigNode[] values() {
            return arr;
        }
    }

    private static class TypeObject implements ConfigObjectNode {
        private final Map<String, ? extends ConfigNode> map;

        private TypeObject(VksTypeDeclaration type) {
            map = type.buildDescendantsMap();
        }

        @Override
        public ConfigNode getNode(String key) {
            return getDescendants().get(key);
        }

        @Override
        public Map<String, ? extends ConfigNode> getDescendants() {
            return map;
        }
    }
}
