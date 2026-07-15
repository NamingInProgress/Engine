package com.vke.core.parsing.config.schema.vks.parser.type;

import com.vke.api.parsing.config.node.ConfigNode;

import java.util.Map;

public interface VksTypeDeclaration {
    Map<String, ? extends ConfigNode> buildDescendantsMap();
}
