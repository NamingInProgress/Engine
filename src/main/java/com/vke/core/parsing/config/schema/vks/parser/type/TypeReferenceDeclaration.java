package com.vke.core.parsing.config.schema.vks.parser.type;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.parsing.config.utils.StringConfigValue;

import java.util.Map;

public class TypeReferenceDeclaration implements VksTypeDeclaration{
    private final String name;

    public TypeReferenceDeclaration(String name) {
        this.name = name;
    }

    @Override
    public Map<String, ? extends ConfigNode> buildDescendantsMap() {
        return Map.of("type", new StringConfigValue(name));
    }
}
