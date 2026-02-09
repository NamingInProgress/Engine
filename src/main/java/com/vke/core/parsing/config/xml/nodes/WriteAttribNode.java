package com.vke.core.parsing.config.xml.nodes;

import com.vke.api.parsing.config.node.AttributedConfigNode;

public interface WriteAttribNode extends AttributedConfigNode {
    void addAttrib(String key, String value);
}
