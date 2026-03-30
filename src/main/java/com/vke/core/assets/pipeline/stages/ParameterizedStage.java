package com.vke.core.assets.pipeline.stages;

import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.EmptyConfigArray;

public abstract class ParameterizedStage implements PipelineStage {
    protected final ConfigArrayNode arguments;

    public ParameterizedStage(ConfigNode node) {
        if (node.hasField("arguments")) {
            arguments = node.getArray("arguments");
        } else {
            arguments = new EmptyConfigArray();
        }
    }
}
